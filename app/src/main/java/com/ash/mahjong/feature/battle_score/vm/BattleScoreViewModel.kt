package com.ash.mahjong.feature.battle_score.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ash.mahjong.LogUtils
import com.ash.mahjong.R
import com.ash.mahjong.data.battle.BattleRecordRepository
import com.ash.mahjong.data.battle.NoOpBattleRecordRepository
import com.ash.mahjong.data.battle.PersistedBattleEventType
import com.ash.mahjong.data.battle.SessionLineupPlayer
import com.ash.mahjong.data.battle.SettledBattleEvent
import com.ash.mahjong.data.battle.SettledBattleRound
import com.ash.mahjong.data.battle.SettledPlayerState
import com.ash.mahjong.data.player.Player
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.player.PlayerRepository
import com.ash.mahjong.data.settings.GameSettings
import com.ash.mahjong.data.settings.GameSettingsRepository
import com.ash.mahjong.feature.battle_score.domain.BattleScoreActionType
import com.ash.mahjong.feature.battle_score.domain.BattleScoreCalculator
import com.ash.mahjong.feature.battle_score.domain.BattleScoreContext
import com.ash.mahjong.feature.battle_score.intent.BattleAction
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.intent.GangType
import com.ash.mahjong.feature.battle_score.state.BattleScoreUiState
import com.ash.mahjong.feature.battle_score.state.DrawSettlementChoiceUiState
import com.ash.mahjong.feature.battle_score.state.DrawSettlementDraftUiState
import com.ash.mahjong.feature.battle_score.state.DrawSettlementStep
import com.ash.mahjong.feature.battle_score.state.EventDraftStep
import com.ash.mahjong.feature.battle_score.state.EventDraftUiState
import com.ash.mahjong.feature.battle_score.state.HorseBindingDraftUiState
import com.ash.mahjong.feature.battle_score.state.HorseUiModel
import com.ash.mahjong.feature.battle_score.state.LiveLogActionType
import com.ash.mahjong.feature.battle_score.state.LiveLogHighlight
import com.ash.mahjong.feature.battle_score.state.LiveLogItemUiModel
import com.ash.mahjong.feature.battle_score.state.LiveLogRelatedPlayerUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.feature.battle_score.state.ResetAllConfirmStep
import com.ash.mahjong.feature.battle_score.state.SettlementPromptType
import com.ash.mahjong.feature.battle_score.state.SettlementPromptUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BattleScoreViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val gameSettingsRepository: GameSettingsRepository,
    private val battleRecordRepository: BattleRecordRepository = NoOpBattleRecordRepository
) : ViewModel() {

    private val scoreCalculator = BattleScoreCalculator()
    private val totalScoreByPlayerId = mutableMapOf<Int, Int>()
    private val roundDeltaByPlayerId = mutableMapOf<Int, Int>()
    private val statusByPlayerId = mutableMapOf<Int, PlayerStatus>()
    private val winOrderByPlayerId = mutableMapOf<Int, Int>()
    private val undoStack = mutableListOf<RoundSnapshot>()
    private val roundScoringHistory = mutableListOf<RoundScoringRecord>()
    private val pendingRoundEvents = mutableListOf<PendingRoundEvent>()

    private var latestPlayers: List<Player> = emptyList()
    private var latestAllPlayers: List<Player> = emptyList()
    private var boundHorseNamesByPlayerId: Map<Int, List<String>> = emptyMap()
    private var latestSettings: GameSettings = GameSettings()
    private var nextLiveLogId: Int = 0
    private var nextWinOrder: Int = 1
    private var nextActionGroupId: Long = 1
    private var currentDealerPlayerId: Int? = null

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<BattleScoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gameSettingsRepository.observeSettings().collect { settings ->
                latestSettings = settings
                _uiState.update { state ->
                    state.copy(
                        hapticsEnabled = settings.hapticsEnabled,
                        multiplierRange = 1..settings.cappingMultiplier,
                        eventDraft = state.eventDraft?.copy(
                            multiplier = state.eventDraft.multiplier?.let { currentMultiplier ->
                                normalizeMultiplier(
                                    multiplier = currentMultiplier,
                                    maxMultiplier = settings.cappingMultiplier
                                )
                            }
                        ),
                        drawSettlementDraft = state.drawSettlementDraft?.let { draft ->
                            draft.copy(
                                currentMultiplier = draft.currentMultiplier?.let { currentMultiplier ->
                                    normalizeMultiplier(
                                        multiplier = currentMultiplier,
                                        maxMultiplier = settings.cappingMultiplier
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            playerRepository.observePlayers().collect { players ->
                val activeOnTablePlayers = selectActiveOnTablePlayers(players)
                val homePlayers = activeOnTablePlayers.take(BATTLE_PLAYER_LIMIT)
                val activeHorses = selectActiveHorses(players)
                val horseUiModels = buildHorseUiModels(
                    horses = activeHorses,
                    activeOnTableById = activeOnTablePlayers.associateBy { player -> player.id }
                )
                latestPlayers = homePlayers
                latestAllPlayers = players
                boundHorseNamesByPlayerId = buildBoundHorseNamesByPlayerId(activeHorses)
                reconcilePlayerState(players = homePlayers)
                ensureCurrentDealerPlayerId(players = homePlayers)
                val requiresSetup = homePlayers.size < BATTLE_PLAYER_LIMIT
                _uiState.update { state ->
                    state.copy(
                        isPlayersLoaded = true,
                        players = buildPlayerUiModels(homePlayers),
                        horses = horseUiModels,
                        horseBindingDraft = state.horseBindingDraft?.takeIf { draft ->
                            horseUiModels.any { horse -> horse.id == draft.horseId }
                        },
                        playerSwapDialogVisible = state.playerSwapDialogVisible &&
                            homePlayers.size == BATTLE_PLAYER_LIMIT &&
                            horseUiModels.isNotEmpty(),
                        requiresPlayerSetup = requiresSetup,
                        canSettle = !requiresSetup,
                        eventDraft = if (requiresSetup) null else state.eventDraft,
                        drawSettlementDraft = if (requiresSetup) null else state.drawSettlementDraft
                    )
                }
            }
        }
    }

    fun onIntent(intent: BattleScoreIntent) {
        LogUtils.d { "Battle score intent: $intent" }
        when (intent) {
            is BattleScoreIntent.SelectAction -> startEventDraft(
                actorId = intent.actorId,
                action = intent.action
            )

            is BattleScoreIntent.SelectTarget -> onSelectTarget(intent.targetId)

            is BattleScoreIntent.SelectGangType -> updateEventDraft { draft ->
                if (draft.step != EventDraftStep.GANG_TYPE) {
                    draft
                } else {
                    draft.copy(
                        gangType = intent.gangType,
                        targetId = null
                    )
                }
            }

            is BattleScoreIntent.SelectMultiplier -> updateEventDraft { draft ->
                if (draft.step != EventDraftStep.MULTIPLIER) {
                    draft
                } else {
                    draft.copy(
                        multiplier = normalizeMultiplier(
                            multiplier = intent.multiplier,
                            maxMultiplier = latestSettings.cappingMultiplier
                        )
                    )
                }
            }

            BattleScoreIntent.ConfirmDraftStep -> confirmDraftStep()
            BattleScoreIntent.BackEventDraftStep -> backEventDraftStep()
            BattleScoreIntent.ConfirmEvent -> confirmEventDraft()
            BattleScoreIntent.CancelEventDraft -> dismissEventDraft()
            BattleScoreIntent.UndoLastEvent -> undoLastEvent()
            BattleScoreIntent.OnFabClick -> onManualSettleClick()
            BattleScoreIntent.OpenResetAllConfirmDialog -> openResetAllConfirmDialog()
            BattleScoreIntent.ConfirmResetAllConfirmDialog -> confirmResetAllConfirmDialog()
            BattleScoreIntent.DismissResetAllConfirmDialog -> dismissResetAllConfirmDialog()
            is BattleScoreIntent.SelectDrawTingChoice -> onSelectDrawTingChoice(intent.isTing)
            is BattleScoreIntent.SelectDrawTingMultiplier -> onSelectDrawTingMultiplier(intent.multiplier)
            BattleScoreIntent.ConfirmDrawSettlementSelection -> confirmDrawSettlementSelection()
            BattleScoreIntent.BackDrawSettlementStep -> backDrawSettlementStep()
            BattleScoreIntent.CancelDrawSettlementDraft -> cancelDrawSettlementDraft()
            BattleScoreIntent.DismissSettlementPrompt -> dismissSettlementPrompt()
            BattleScoreIntent.ConfirmSettleAndNextRound -> confirmSettlementAndNextRound()
            is BattleScoreIntent.StartHorseBinding -> startHorseBinding(intent.horseId)
            is BattleScoreIntent.SelectHorseBindingTarget -> {
                selectHorseBindingTarget(intent.targetPlayerId)
            }
            BattleScoreIntent.CancelHorseBinding -> cancelHorseBinding()
            BattleScoreIntent.OpenPlayerSwapDialog -> openPlayerSwapDialog()
            BattleScoreIntent.DismissPlayerSwapDialog -> dismissPlayerSwapDialog()
            is BattleScoreIntent.SwapOnTableWithHorse -> {
                swapOnTableWithHorse(
                    onTablePlayerId = intent.onTablePlayerId,
                    horsePlayerId = intent.horsePlayerId
                )
            }
        }

        _uiState.update { state ->
            state.copy(lastAction = intent::class.simpleName)
        }
    }

    private fun startEventDraft(actorId: Int, action: BattleAction) {
        if (uiState.value.requiresPlayerSetup || !canStartActionDraft(actorId, action)) {
            return
        }
        _uiState.update { state ->
            state.copy(
                eventDraft = EventDraftUiState(
                    step = initialDraftStep(action),
                    action = action,
                    actorId = actorId,
                    gangType = null,
                    targetId = null,
                    multiplier = null
                )
            )
        }
    }

    private fun updateEventDraft(transform: (EventDraftUiState) -> EventDraftUiState) {
        _uiState.update { state ->
            val currentDraft = state.eventDraft ?: return@update state
            state.copy(eventDraft = transform(currentDraft))
        }
    }

    private fun dismissEventDraft() {
        _uiState.update { state -> state.copy(eventDraft = null) }
    }

    private fun startHorseBinding(horseId: Int) {
        val horse = latestAllPlayers.firstOrNull { player ->
            player.id == horseId &&
                player.isActive &&
                player.playerRole == PlayerRole.HORSE
        } ?: return

        _uiState.update { state ->
            state.copy(
                horseBindingDraft = HorseBindingDraftUiState(
                    horseId = horse.id,
                    horseName = horse.name
                )
            )
        }
    }

    private fun selectHorseBindingTarget(targetPlayerId: Int) {
        val draft = uiState.value.horseBindingDraft ?: return
        val allowedTargetIds = uiState.value.players.map { player -> player.id }.toSet()
        if (targetPlayerId !in allowedTargetIds) {
            return
        }

        viewModelScope.launch {
            playerRepository.updateHorseBinding(
                playerId = draft.horseId,
                boundOnTablePlayerId = targetPlayerId
            )
            recordRoundEvent(
                event = PendingRoundEvent(
                    actionGroupId = nextActionGroupId++,
                    eventType = PersistedBattleEventType.HORSE_BIND,
                    actorPlayerId = draft.horseId,
                    multiplier = null,
                    payloadJson = buildHorseBindingPayloadJson(
                        horseId = draft.horseId,
                        targetPlayerId = targetPlayerId
                    ),
                    deltaByPlayerId = emptyMap()
                )
            )
            updateLatestHorseBindingLocally(
                horseId = draft.horseId,
                boundOnTablePlayerId = targetPlayerId
            )
            _uiState.update { state ->
                state.copy(
                    players = buildPlayerUiModels(latestPlayers),
                    horses = buildCurrentHorseUiModels(),
                    horseBindingDraft = null
                )
            }
        }
    }

    private fun cancelHorseBinding() {
        _uiState.update { state -> state.copy(horseBindingDraft = null) }
    }

    private fun openPlayerSwapDialog() {
        val state = uiState.value
        if (state.requiresPlayerSetup || state.players.size < BATTLE_PLAYER_LIMIT || state.horses.isEmpty()) {
            return
        }
        _uiState.update { current -> current.copy(playerSwapDialogVisible = true) }
    }

    private fun dismissPlayerSwapDialog() {
        _uiState.update { state -> state.copy(playerSwapDialogVisible = false) }
    }

    private fun swapOnTableWithHorse(
        onTablePlayerId: Int,
        horsePlayerId: Int
    ) {
        val onTablePlayer = latestAllPlayers.firstOrNull { player ->
            player.id == onTablePlayerId &&
                player.isActive &&
                player.playerRole == PlayerRole.ON_TABLE
        } ?: return
        val horsePlayer = latestAllPlayers.firstOrNull { player ->
            player.id == horsePlayerId &&
                player.isActive &&
                player.playerRole == PlayerRole.HORSE
        } ?: return

        viewModelScope.launch {
            playerRepository.swapOnTableWithHorse(
                onTablePlayerId = onTablePlayer.id,
                horsePlayerId = horsePlayer.id
            )
            _uiState.update { state ->
                state.copy(
                    horseBindingDraft = null,
                    eventDraft = null,
                    drawSettlementDraft = null
                )
            }
        }
    }

    private fun onSelectTarget(targetId: Int) {
        val draft = uiState.value.eventDraft ?: return
        if (draft.step != EventDraftStep.TARGET) return
        if (targetId !in activeOpponentIds(draft.actorId)) return

        when (draft.action) {
            BattleAction.HU -> {
                updateEventDraft {
                    it.copy(
                        targetId = targetId,
                        step = EventDraftStep.MULTIPLIER,
                        multiplier = null
                    )
                }
            }

            BattleAction.GANG -> {
                applyScoringForDraft(
                    draft = draft.copy(targetId = targetId),
                    selectedMultiplier = FIXED_GANG_MULTIPLIER
                )
            }

            BattleAction.ZIMO -> Unit
        }
    }

    private fun confirmDraftStep() {
        val draft = uiState.value.eventDraft ?: return
        if (draft.step != EventDraftStep.GANG_TYPE) return
        val selectedGangType = draft.gangType ?: return
        when (selectedGangType) {
            GangType.DIAN -> updateEventDraft {
                it.copy(
                    step = EventDraftStep.TARGET,
                    targetId = null,
                    multiplier = null
                )
            }

            GangType.BA,
            GangType.AN -> {
                applyScoringForDraft(
                    draft = draft,
                    selectedMultiplier = FIXED_GANG_MULTIPLIER
                )
            }
        }
    }

    private fun backEventDraftStep() {
        val draft = uiState.value.eventDraft ?: return
        when (draft.step) {
            EventDraftStep.GANG_TYPE -> dismissEventDraft()
            EventDraftStep.TARGET -> {
                when (draft.action) {
                    BattleAction.HU -> dismissEventDraft()
                    BattleAction.GANG -> updateEventDraft {
                        it.copy(
                            step = EventDraftStep.GANG_TYPE,
                            targetId = null,
                            multiplier = null
                        )
                    }
                    BattleAction.ZIMO -> dismissEventDraft()
                }
            }
            EventDraftStep.MULTIPLIER -> {
                when (draft.action) {
                    BattleAction.HU -> updateEventDraft {
                        it.copy(step = EventDraftStep.TARGET)
                    }
                    BattleAction.GANG -> dismissEventDraft()
                    BattleAction.ZIMO -> dismissEventDraft()
                }
            }
        }
    }

    private fun confirmEventDraft() {
        val draft = uiState.value.eventDraft ?: return
        if (draft.step != EventDraftStep.MULTIPLIER) return
        val selectedMultiplier = draft.multiplier ?: return
        applyScoringForDraft(
            draft = draft,
            selectedMultiplier = selectedMultiplier
        )
    }

    private fun applyScoringForDraft(
        draft: EventDraftUiState,
        selectedMultiplier: Int
    ) {
        if (!canApplyScoringDraft(draft)) {
            dismissEventDraft()
            return
        }

        val activeTargetIds = activeOpponentIds(actorId = draft.actorId)
        val scoringAction = when (draft.action) {
            BattleAction.HU -> {
                val targetId = draft.targetId ?: return
                if (targetId !in activeTargetIds) return
                PendingScoringAction(
                    actionType = BattleScoreActionType.HU,
                    actorStatus = PlayerStatus.HU,
                    targetIds = listOf(targetId)
                )
            }

            BattleAction.ZIMO -> {
                if (activeTargetIds.isEmpty()) return
                PendingScoringAction(
                    actionType = BattleScoreActionType.ZIMO,
                    actorStatus = PlayerStatus.ZIMO,
                    targetIds = activeTargetIds
                )
            }

            BattleAction.GANG -> {
                when (draft.gangType) {
                    GangType.DIAN -> {
                        val targetId = draft.targetId ?: return
                        if (targetId !in activeTargetIds) return
                        PendingScoringAction(
                            actionType = BattleScoreActionType.GANG_DIAN,
                            actorStatus = actorStatusAfterGang(actorId = draft.actorId),
                            targetIds = listOf(targetId)
                        )
                    }

                    GangType.BA -> {
                        if (activeTargetIds.isEmpty()) return
                        PendingScoringAction(
                            actionType = BattleScoreActionType.GANG_BA,
                            actorStatus = actorStatusAfterGang(actorId = draft.actorId),
                            targetIds = activeTargetIds
                        )
                    }

                    GangType.AN -> {
                        if (activeTargetIds.isEmpty()) return
                        PendingScoringAction(
                            actionType = BattleScoreActionType.GANG_AN,
                            actorStatus = actorStatusAfterGang(actorId = draft.actorId),
                            targetIds = activeTargetIds
                        )
                    }

                    null -> return
                }
            }
        }

        val baseDeltaByPlayerId = scoreCalculator.calculateDelta(
            actionType = scoringAction.actionType,
            context = BattleScoreContext(
                actorId = draft.actorId,
                targetIds = scoringAction.targetIds,
                multiplier = if (scoringAction.actionType == BattleScoreActionType.GANG_DIAN ||
                    scoringAction.actionType == BattleScoreActionType.GANG_BA ||
                    scoringAction.actionType == BattleScoreActionType.GANG_AN
                ) {
                    FIXED_GANG_MULTIPLIER
                } else {
                    selectedMultiplier
                }
            ),
            settings = latestSettings
        )
        val deltaByPlayerId = applyHorseFollowDelta(
            baseDeltaByPlayerId = baseDeltaByPlayerId,
            actorId = draft.actorId,
            targetIds = scoringAction.targetIds
        )
        if (deltaByPlayerId.isEmpty()) return

        pushUndoSnapshot()

        deltaByPlayerId.forEach { (playerId, delta) ->
            totalScoreByPlayerId[playerId] = (totalScoreByPlayerId[playerId] ?: 0) + delta
            roundDeltaByPlayerId[playerId] = (roundDeltaByPlayerId[playerId] ?: 0) + delta
        }
        statusByPlayerId[draft.actorId] = scoringAction.actorStatus
        assignWinOrderIfNeeded(
            playerId = draft.actorId,
            status = scoringAction.actorStatus
        )
        roundScoringHistory.add(
            RoundScoringRecord(
                actionType = scoringAction.actionType,
                actorId = draft.actorId,
                deltaByPlayerId = deltaByPlayerId
            )
        )
        recordRoundEvent(
            event = PendingRoundEvent(
                actionGroupId = nextActionGroupId++,
                eventType = scoringAction.actionType.toPersistedEventType(),
                actorPlayerId = draft.actorId,
                multiplier = selectedMultiplier,
                payloadJson = buildScoringPayloadJson(
                    targetIds = scoringAction.targetIds
                ),
                deltaByPlayerId = deltaByPlayerId
            )
        )

        val scoringLogs = buildScoringLiveLogs(
            actorId = draft.actorId,
            actionType = scoringAction.toLiveLogActionType(),
            fallbackTargetIds = scoringAction.targetIds,
            deltaByPlayerId = deltaByPlayerId
        )
        val updatedLogs = (scoringLogs + uiState.value.liveLogs).take(MAX_LOG_COUNT)
        val settlementPrompt = if (huPlayerCount() >= AUTO_SETTLE_HU_COUNT) {
            SettlementPromptUiState(type = SettlementPromptType.AUTO_THREE_HU)
        } else {
            null
        }

        _uiState.update { state ->
            state.copy(
                players = buildPlayerUiModels(latestPlayers),
                horses = buildCurrentHorseUiModels(),
                liveLogs = updatedLogs,
                eventDraft = null,
                canUndo = undoStack.isNotEmpty(),
                settlementPrompt = settlementPrompt
            )
        }
    }

    private fun onManualSettleClick() {
        if (uiState.value.requiresPlayerSetup) {
            return
        }
        if (huPlayerCount() >= AUTO_SETTLE_HU_COUNT) {
            _uiState.update { state ->
                state.copy(
                    eventDraft = null,
                    drawSettlementDraft = null,
                    settlementPrompt = SettlementPromptUiState(type = SettlementPromptType.AUTO_THREE_HU)
                )
            }
            return
        }
        startDrawSettlementDraft()
    }

    private fun openResetAllConfirmDialog() {
        if (uiState.value.requiresPlayerSetup) {
            return
        }
        _uiState.update { state ->
            state.copy(
                eventDraft = null,
                drawSettlementDraft = null,
                settlementPrompt = null,
                horseBindingDraft = null,
                playerSwapDialogVisible = false,
                resetAllConfirmStep = ResetAllConfirmStep.FIRST
            )
        }
    }

    private fun dismissResetAllConfirmDialog() {
        _uiState.update { state -> state.copy(resetAllConfirmStep = null) }
    }

    private fun confirmResetAllConfirmDialog() {
        when (uiState.value.resetAllConfirmStep) {
            ResetAllConfirmStep.FIRST -> {
                _uiState.update { state -> state.copy(resetAllConfirmStep = ResetAllConfirmStep.SECOND) }
            }

            ResetAllConfirmStep.SECOND -> {
                resetAllRoundsAndScores()
            }

            null -> Unit
        }
    }

    private fun resetAllRoundsAndScores() {
        totalScoreByPlayerId.clear()
        roundDeltaByPlayerId.clear()
        statusByPlayerId.clear()
        winOrderByPlayerId.clear()

        latestAllPlayers
            .filter { player -> player.isActive }
            .forEach { player ->
                totalScoreByPlayerId[player.id] = player.score
                roundDeltaByPlayerId[player.id] = 0
            }

        latestPlayers.forEach { player ->
            statusByPlayerId[player.id] = PlayerStatus.ACTIVE
        }

        nextWinOrder = 1
        nextLiveLogId = 0
        nextActionGroupId = 1
        currentDealerPlayerId = latestPlayers.firstOrNull()?.id
        undoStack.clear()
        roundScoringHistory.clear()
        pendingRoundEvents.clear()

        _uiState.update { state ->
            state.copy(
                currentRound = 1,
                players = buildPlayerUiModels(latestPlayers),
                horses = buildCurrentHorseUiModels(),
                liveLogs = emptyList(),
                canUndo = false,
                eventDraft = null,
                drawSettlementDraft = null,
                settlementPrompt = null,
                horseBindingDraft = null,
                playerSwapDialogVisible = false,
                resetAllConfirmStep = null
            )
        }
    }

    private fun startDrawSettlementDraft() {
        val orderedPendingPlayerIds = latestPlayers
            .map { it.id }
            .filter { playerId -> !isHuCompleted(playerId) }
        if (orderedPendingPlayerIds.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    eventDraft = null,
                    drawSettlementDraft = null,
                    settlementPrompt = SettlementPromptUiState(
                        type = SettlementPromptType.DRAW_RESULT_CONFIRM
                    )
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                eventDraft = null,
                settlementPrompt = null,
                drawSettlementDraft = DrawSettlementDraftUiState(
                    orderedPendingPlayerIds = orderedPendingPlayerIds,
                    currentIndex = 0,
                    choicesByPlayerId = emptyMap(),
                    step = DrawSettlementStep.CHOOSE_TING,
                    currentTingChoice = null,
                    currentMultiplier = null
                )
            )
        }
    }

    private fun onSelectDrawTingChoice(isTing: Boolean) {
        _uiState.update { state ->
            val draft = state.drawSettlementDraft ?: return@update state
            if (draft.step != DrawSettlementStep.CHOOSE_TING) return@update state
            state.copy(
                drawSettlementDraft = draft.copy(
                    currentTingChoice = isTing,
                    step = if (isTing) DrawSettlementStep.CHOOSE_MULTIPLIER else DrawSettlementStep.CHOOSE_TING,
                    currentMultiplier = if (isTing) {
                        draft.currentMultiplier ?: buildMultiplierOptions(latestSettings.cappingMultiplier).firstOrNull()
                    } else {
                        null
                    }
                )
            )
        }
    }

    private fun onSelectDrawTingMultiplier(multiplier: Int) {
        _uiState.update { state ->
            val draft = state.drawSettlementDraft ?: return@update state
            if (draft.step != DrawSettlementStep.CHOOSE_MULTIPLIER) return@update state
            state.copy(
                drawSettlementDraft = draft.copy(
                    currentMultiplier = normalizeMultiplier(
                        multiplier = multiplier,
                        maxMultiplier = latestSettings.cappingMultiplier
                    )
                )
            )
        }
    }

    private fun confirmDrawSettlementSelection() {
        val draft = uiState.value.drawSettlementDraft ?: return
        val currentPlayerId = draft.orderedPendingPlayerIds.getOrNull(draft.currentIndex) ?: return
        val currentChoice = draft.currentTingChoice ?: return
        val choice = DrawSettlementChoiceUiState(
            isTing = currentChoice,
            multiplier = if (currentChoice) {
                draft.currentMultiplier ?: return
            } else {
                null
            }
        )
        val updatedChoices = draft.choicesByPlayerId + (currentPlayerId to choice)
        val nextIndex = draft.currentIndex + 1
        if (nextIndex < draft.orderedPendingPlayerIds.size) {
            _uiState.update { state ->
                val latestDraft = state.drawSettlementDraft ?: return@update state
                state.copy(
                    drawSettlementDraft = latestDraft.copy(
                        currentIndex = nextIndex,
                        choicesByPlayerId = updatedChoices,
                        step = DrawSettlementStep.CHOOSE_TING,
                        currentTingChoice = null,
                        currentMultiplier = null
                    )
                )
            }
        } else {
            finalizeDrawSettlement(
                choicesByPlayerId = updatedChoices,
                orderedPendingPlayerIds = draft.orderedPendingPlayerIds
            )
        }
    }

    private fun backDrawSettlementStep() {
        _uiState.update { state ->
            val draft = state.drawSettlementDraft ?: return@update state
            if (draft.step == DrawSettlementStep.CHOOSE_MULTIPLIER) {
                return@update state.copy(
                    drawSettlementDraft = draft.copy(
                        step = DrawSettlementStep.CHOOSE_TING,
                        currentTingChoice = true,
                        currentMultiplier = null
                    )
                )
            }
            if (draft.currentIndex == 0) {
                return@update state.copy(drawSettlementDraft = null)
            }
            val previousIndex = draft.currentIndex - 1
            val previousPlayerId = draft.orderedPendingPlayerIds.getOrNull(previousIndex) ?: return@update state
            val previousChoice = draft.choicesByPlayerId[previousPlayerId] ?: return@update state
            val revertedChoices = draft.choicesByPlayerId - previousPlayerId
            state.copy(
                drawSettlementDraft = draft.copy(
                    currentIndex = previousIndex,
                    choicesByPlayerId = revertedChoices,
                    step = if (previousChoice.isTing) {
                        DrawSettlementStep.CHOOSE_MULTIPLIER
                    } else {
                        DrawSettlementStep.CHOOSE_TING
                    },
                    currentTingChoice = previousChoice.isTing,
                    currentMultiplier = previousChoice.multiplier
                )
            )
        }
    }

    private fun cancelDrawSettlementDraft() {
        _uiState.update { state -> state.copy(drawSettlementDraft = null) }
    }

    private fun finalizeDrawSettlement(
        choicesByPlayerId: Map<Int, DrawSettlementChoiceUiState>,
        orderedPendingPlayerIds: List<Int>
    ) {
        val tingPlayerIds = orderedPendingPlayerIds.filter { choicesByPlayerId[it]?.isTing == true }
        val nonTingPlayerIds = orderedPendingPlayerIds.filter { choicesByPlayerId[it]?.isTing != true }
        val nonTingPlayerIdSet = nonTingPlayerIds.toSet()
        val drawScoreByTingPlayerId = tingPlayerIds.associateWith { playerId ->
            val multiplier = choicesByPlayerId[playerId]?.multiplier ?: 1
            latestSettings.basePoint * multiplier
        }

        val deltaByPlayerId = mutableMapOf<Int, Int>()
        val refundedGangRecords = roundScoringHistory
            .filter { record ->
                record.actionType.isGangType() &&
                    record.actorId in nonTingPlayerIdSet
            }
        refundedGangRecords.forEach { record ->
            record.deltaByPlayerId.forEach { (playerId, delta) ->
                // 流局未听牌退杠：整笔杠分（含马儿关联）按原路径回滚。
                deltaByPlayerId[playerId] = (deltaByPlayerId[playerId] ?: 0) - delta
            }
        }
        if (tingPlayerIds.isNotEmpty() && nonTingPlayerIds.isNotEmpty()) {
            val totalTingScore = drawScoreByTingPlayerId.values.sum()
            nonTingPlayerIds.forEach { playerId ->
                deltaByPlayerId[playerId] = (deltaByPlayerId[playerId] ?: 0) - totalTingScore
            }
            drawScoreByTingPlayerId.forEach { (playerId, score) ->
                val gain = score * nonTingPlayerIds.size
                deltaByPlayerId[playerId] = (deltaByPlayerId[playerId] ?: 0) + gain
            }
        }

        pushUndoSnapshot()

        deltaByPlayerId.forEach { (playerId, delta) ->
            totalScoreByPlayerId[playerId] = (totalScoreByPlayerId[playerId] ?: 0) + delta
            roundDeltaByPlayerId[playerId] = (roundDeltaByPlayerId[playerId] ?: 0) + delta
        }
        if (refundedGangRecords.isNotEmpty()) {
            roundScoringHistory.removeAll(refundedGangRecords.toSet())
        }

        val gangRefundLogs = refundedGangRecords.flatMap { record ->
            val refundedDeltaByPlayerId = record.deltaByPlayerId.mapValues { (_, delta) -> -delta }
            buildScoringLiveLogs(
                actorId = record.actorId,
                actionType = LiveLogActionType.GANG_REFUND,
                fallbackTargetIds = refundedDeltaByPlayerId.keys
                    .filter { playerId -> playerId != record.actorId },
                deltaByPlayerId = refundedDeltaByPlayerId
            )
        }

        val actorId = tingPlayerIds.firstOrNull() ?: orderedPendingPlayerIds.firstOrNull()
        val relatedPlayerIds = if (tingPlayerIds.isNotEmpty()) {
            tingPlayerIds + nonTingPlayerIds
        } else {
            orderedPendingPlayerIds
        }.filter { it != actorId }

        val drawLogs = actorId?.let { drawActorId ->
            buildScoringLiveLogs(
                actorId = drawActorId,
                actionType = LiveLogActionType.DRAW_SETTLEMENT,
                fallbackTargetIds = relatedPlayerIds,
                deltaByPlayerId = deltaByPlayerId
            )
        }.orEmpty()

        val settlementLogs = buildList {
            addAll(drawLogs)
            addAll(gangRefundLogs)
        }
        val settlementActionGroupId = nextActionGroupId++
        refundedGangRecords.forEach { record ->
            val refundedDeltaByPlayerId = record.deltaByPlayerId.mapValues { (_, delta) -> -delta }
            recordRoundEvent(
                event = PendingRoundEvent(
                    actionGroupId = settlementActionGroupId,
                    eventType = PersistedBattleEventType.GANG_REFUND,
                    actorPlayerId = record.actorId,
                    multiplier = FIXED_GANG_MULTIPLIER,
                    payloadJson = buildScoringPayloadJson(
                        targetIds = record.deltaByPlayerId.keys
                            .filter { playerId -> playerId != record.actorId }
                    ),
                    deltaByPlayerId = refundedDeltaByPlayerId
                )
            )
        }
        val drawDeltaByPlayerId = orderedPendingPlayerIds.associateWith { playerId ->
            deltaByPlayerId[playerId] ?: 0
        }
        if (drawDeltaByPlayerId.isNotEmpty()) {
            recordRoundEvent(
                event = PendingRoundEvent(
                    actionGroupId = settlementActionGroupId,
                    eventType = PersistedBattleEventType.DRAW_SETTLEMENT,
                    actorPlayerId = actorId,
                    multiplier = null,
                    payloadJson = buildDrawSettlementPayloadJson(
                        tingPlayerIds = tingPlayerIds,
                        nonTingPlayerIds = nonTingPlayerIds
                    ),
                    deltaByPlayerId = drawDeltaByPlayerId
                )
            )
        }
        val updatedLogs = if (settlementLogs.isEmpty()) {
            uiState.value.liveLogs
        } else {
            (settlementLogs + uiState.value.liveLogs).take(MAX_LOG_COUNT)
        }

        _uiState.update { state ->
            state.copy(
                players = buildPlayerUiModels(latestPlayers),
                horses = buildCurrentHorseUiModels(),
                liveLogs = updatedLogs,
                canUndo = undoStack.isNotEmpty(),
                eventDraft = null,
                drawSettlementDraft = null,
                settlementPrompt = SettlementPromptUiState(type = SettlementPromptType.DRAW_RESULT_CONFIRM)
            )
        }
    }

    private fun dismissSettlementPrompt() {
        _uiState.update { state -> state.copy(settlementPrompt = null) }
    }

    private fun confirmSettlementAndNextRound() {
        val prompt = uiState.value.settlementPrompt ?: return
        if (prompt.type != SettlementPromptType.AUTO_THREE_HU &&
            prompt.type != SettlementPromptType.DRAW_RESULT_CONFIRM
        ) {
            dismissSettlementPrompt()
            return
        }
        viewModelScope.launch {
            try {
                persistCurrentRound()
            } catch (e: Exception) {
                LogUtils.e(throwable = e) { "Persist settled round failed" }
            }
            startNextRound()
        }
    }

    private fun startNextRound() {
        clearHorseBindingsForNextRound()
        currentDealerPlayerId = firstWinningPlayerIdOfCurrentRound()
            ?: currentDealerPlayerId
            ?: latestPlayers.firstOrNull()?.id

        roundDeltaByPlayerId.keys.toList().forEach { playerId ->
            roundDeltaByPlayerId[playerId] = 0
        }
        statusByPlayerId.keys.toList().forEach { playerId ->
            statusByPlayerId[playerId] = PlayerStatus.ACTIVE
        }
        winOrderByPlayerId.clear()
        nextWinOrder = 1
        nextActionGroupId = 1
        undoStack.clear()
        roundScoringHistory.clear()
        pendingRoundEvents.clear()

        _uiState.update { state ->
            state.copy(
                currentRound = state.currentRound + 1,
                players = buildPlayerUiModels(latestPlayers),
                horses = buildCurrentHorseUiModels(),
                liveLogs = emptyList(),
                canUndo = false,
                eventDraft = null,
                drawSettlementDraft = null,
                settlementPrompt = null,
                horseBindingDraft = null
            )
        }
    }

    private fun clearHorseBindingsForNextRound() {
        val horseIdsWithBinding = latestAllPlayers
            .filter { player ->
                player.playerRole == PlayerRole.HORSE &&
                    player.boundOnTablePlayerId != null
            }
            .map { player -> player.id }
        if (horseIdsWithBinding.isEmpty()) {
            return
        }
        horseIdsWithBinding.forEach { horseId ->
            updateLatestHorseBindingLocally(
                horseId = horseId,
                boundOnTablePlayerId = null
            )
        }

        viewModelScope.launch {
            horseIdsWithBinding.forEach { horseId ->
                playerRepository.updateHorseBinding(
                    playerId = horseId,
                    boundOnTablePlayerId = null
                )
            }
        }
    }

    private fun undoLastEvent() {
        val snapshot = undoStack.removeLastOrNull() ?: return
        totalScoreByPlayerId.clear()
        totalScoreByPlayerId.putAll(snapshot.totalScoreByPlayerId)
        roundDeltaByPlayerId.clear()
        roundDeltaByPlayerId.putAll(snapshot.roundDeltaByPlayerId)
        statusByPlayerId.clear()
        statusByPlayerId.putAll(snapshot.statusByPlayerId)
        winOrderByPlayerId.clear()
        winOrderByPlayerId.putAll(snapshot.winOrderByPlayerId)
        roundScoringHistory.clear()
        roundScoringHistory.addAll(snapshot.roundScoringHistory)
        pendingRoundEvents.clear()
        pendingRoundEvents.addAll(snapshot.pendingRoundEvents)
        nextLiveLogId = snapshot.nextLiveLogId
        nextWinOrder = snapshot.nextWinOrder
        nextActionGroupId = snapshot.nextActionGroupId

        _uiState.update { state ->
            state.copy(
                players = buildPlayerUiModels(latestPlayers),
                horses = buildCurrentHorseUiModels(),
                liveLogs = snapshot.liveLogs,
                canUndo = undoStack.isNotEmpty(),
                eventDraft = null,
                drawSettlementDraft = snapshot.drawSettlementDraft,
                settlementPrompt = snapshot.settlementPrompt
            )
        }
    }

    private fun pushUndoSnapshot() {
        undoStack.add(
            RoundSnapshot(
                totalScoreByPlayerId = totalScoreByPlayerId.toMap(),
                roundDeltaByPlayerId = roundDeltaByPlayerId.toMap(),
                statusByPlayerId = statusByPlayerId.toMap(),
                winOrderByPlayerId = winOrderByPlayerId.toMap(),
                roundScoringHistory = roundScoringHistory.toList(),
                pendingRoundEvents = pendingRoundEvents.toList(),
                liveLogs = uiState.value.liveLogs,
                drawSettlementDraft = uiState.value.drawSettlementDraft,
                settlementPrompt = uiState.value.settlementPrompt,
                nextLiveLogId = nextLiveLogId,
                nextWinOrder = nextWinOrder,
                nextActionGroupId = nextActionGroupId
            )
        )
    }

    private suspend fun persistCurrentRound() {
        if (pendingRoundEvents.isEmpty()) {
            return
        }
        val onTablePlayers = selectActiveOnTablePlayers(latestAllPlayers)
        if (onTablePlayers.size < BATTLE_PLAYER_LIMIT) {
            return
        }
        val activeHorses = selectActiveHorses(latestAllPlayers)
        val lineupPlayers = buildList {
            onTablePlayers.forEachIndexed { index, player ->
                add(
                    SessionLineupPlayer(
                        playerId = player.id,
                        seatIndex = index,
                        playerRole = player.playerRole.name,
                        nameSnapshot = player.name,
                        avatarKeySnapshot = player.avatarKey,
                        initialScore = player.score
                    )
                )
            }
            activeHorses.forEachIndexed { index, player ->
                add(
                    SessionLineupPlayer(
                        playerId = player.id,
                        seatIndex = onTablePlayers.size + index,
                        playerRole = player.playerRole.name,
                        nameSnapshot = player.name,
                        avatarKeySnapshot = player.avatarKey,
                        initialScore = player.score
                    )
                )
            }
        }
        val activePlayers = latestAllPlayers.filter { player -> player.isActive }
        val playerStates = activePlayers.map { player ->
            SettledPlayerState(
                playerId = player.id,
                totalScore = totalScoreByPlayerId[player.id] ?: player.score,
                roundDelta = roundDeltaByPlayerId[player.id] ?: 0,
                status = statusByPlayerId[player.id]?.name ?: PlayerStatus.ACTIVE.name,
                winOrder = winOrderByPlayerId[player.id]
            )
        }
        battleRecordRepository.persistSettledRound(
            SettledBattleRound(
                roundNo = uiState.value.currentRound,
                lineupPlayers = lineupPlayers,
                basePoint = latestSettings.basePoint,
                cappingMultiplier = latestSettings.cappingMultiplier,
                events = pendingRoundEvents.map { event ->
                    SettledBattleEvent(
                        actionGroupId = event.actionGroupId,
                        eventType = event.eventType,
                        actorPlayerId = event.actorPlayerId,
                        multiplier = event.multiplier,
                        payloadJson = event.payloadJson,
                        deltaByPlayerId = event.deltaByPlayerId
                    )
                },
                playerStates = playerStates
            )
        )
    }

    private fun recordRoundEvent(event: PendingRoundEvent) {
        pendingRoundEvents.add(event)
    }

    private fun buildScoringPayloadJson(targetIds: List<Int>): String {
        val targets = targetIds.joinToString(separator = ",")
        return "{\"targetIds\":[$targets]}"
    }

    private fun buildHorseBindingPayloadJson(horseId: Int, targetPlayerId: Int): String {
        return "{\"horseId\":$horseId,\"targetPlayerId\":$targetPlayerId}"
    }

    private fun buildDrawSettlementPayloadJson(
        tingPlayerIds: List<Int>,
        nonTingPlayerIds: List<Int>
    ): String {
        val tingIds = tingPlayerIds.joinToString(separator = ",")
        val nonTingIds = nonTingPlayerIds.joinToString(separator = ",")
        return "{\"tingPlayerIds\":[$tingIds],\"nonTingPlayerIds\":[$nonTingIds]}"
    }

    private fun createInitialState(): BattleScoreUiState {
        return BattleScoreUiState(
            currentRound = 1,
            windLabelRes = R.string.battle_wind_east,
            isPlayersLoaded = false,
            players = emptyList(),
            horses = emptyList(),
            liveLogs = emptyList(),
            hapticsEnabled = GameSettings.DEFAULT_HAPTICS_ENABLED,
            lastAction = null,
            requiresPlayerSetup = true,
            canUndo = false,
            canSettle = false,
            playerSwapDialogVisible = false,
            multiplierRange = 1..GameSettings.DEFAULT_CAPPING_MULTIPLIER,
            eventDraft = null,
            drawSettlementDraft = null,
            settlementPrompt = null,
            resetAllConfirmStep = null
        )
    }

    private fun selectActiveOnTablePlayers(players: List<Player>): List<Player> {
        return players
            .filter { it.isActive && it.playerRole == PlayerRole.ON_TABLE }
            .sortedWith(compareBy<Player> { it.createdAt }.thenBy { it.id })
    }

    private fun selectActiveHorses(players: List<Player>): List<Player> {
        return players
            .filter { it.isActive && it.playerRole == PlayerRole.HORSE }
            .sortedWith(compareBy<Player> { it.createdAt }.thenBy { it.id })
    }

    private fun reconcilePlayerState(players: List<Player>) {
        val latestIds = players.map { it.id }.toSet()
        val scoreTrackedIds = latestAllPlayers
            .asSequence()
            .filter { player -> player.isActive }
            .map { player -> player.id }
            .toSet()
        totalScoreByPlayerId.keys.retainAll(scoreTrackedIds)
        roundDeltaByPlayerId.keys.retainAll(scoreTrackedIds)
        statusByPlayerId.keys.retainAll(latestIds)
        winOrderByPlayerId.keys.retainAll(latestIds)
        nextWinOrder = (winOrderByPlayerId.values.maxOrNull() ?: 0) + 1

        latestAllPlayers
            .filter { player ->
                player.id in scoreTrackedIds
            }
            .forEach { player ->
            totalScoreByPlayerId.putIfAbsent(player.id, player.score)
            roundDeltaByPlayerId.putIfAbsent(player.id, 0)
        }

        players.forEach { player ->
            statusByPlayerId.putIfAbsent(player.id, PlayerStatus.ACTIVE)
        }
    }

    private fun applyHorseFollowDelta(
        baseDeltaByPlayerId: Map<Int, Int>,
        actorId: Int,
        targetIds: List<Int>
    ): Map<Int, Int> {
        if (baseDeltaByPlayerId.isEmpty()) {
            return emptyMap()
        }

        val transferEdges = buildBaseTransferEdges(
            baseDeltaByPlayerId = baseDeltaByPlayerId,
            actorId = actorId,
            targetIds = targetIds
        )
        if (transferEdges.isEmpty()) {
            return baseDeltaByPlayerId
        }
        val boundHorseIdsByPlayerId = buildBoundHorseIdsByPlayerId()
        val expandedTransferEdges = buildList {
            transferEdges.forEach { edge ->
                val payerIds = listOf(edge.payerId) + boundHorseIdsByPlayerId[edge.payerId].orEmpty()
                val receiverIds = listOf(edge.receiverId) + boundHorseIdsByPlayerId[edge.receiverId].orEmpty()
                payerIds.forEach { payerId ->
                    receiverIds.forEach { receiverId ->
                        add(
                            TransferEdge(
                                payerId = payerId,
                                receiverId = receiverId,
                                amount = edge.amount
                            )
                        )
                    }
                }
            }
        }
        return buildDeltaByPlayerId(expandedTransferEdges)
    }

    private fun buildBaseTransferEdges(
        baseDeltaByPlayerId: Map<Int, Int>,
        actorId: Int,
        targetIds: List<Int>
    ): List<TransferEdge> {
        val normalizedTargetIds = targetIds
            .distinct()
            .filter { targetId -> targetId != actorId }
        return normalizedTargetIds.mapNotNull { targetId ->
            val amount = -(baseDeltaByPlayerId[targetId] ?: 0)
            if (amount <= 0) {
                null
            } else {
                TransferEdge(
                    payerId = targetId,
                    receiverId = actorId,
                    amount = amount
                )
            }
        }
    }

    private fun buildBoundHorseIdsByPlayerId(): Map<Int, List<Int>> {
        val activeOnTablePlayerIds = latestAllPlayers
            .asSequence()
            .filter { player -> player.isActive && player.playerRole == PlayerRole.ON_TABLE }
            .map { player -> player.id }
            .toSet()
        return latestAllPlayers
            .asSequence()
            .filter { player -> player.isActive && player.playerRole == PlayerRole.HORSE }
            .mapNotNull { horse ->
                val boundPlayerId = horse.boundOnTablePlayerId ?: return@mapNotNull null
                if (boundPlayerId !in activeOnTablePlayerIds) {
                    return@mapNotNull null
                }
                boundPlayerId to horse.id
            }
            .groupBy(
                keySelector = { (boundPlayerId, _) -> boundPlayerId },
                valueTransform = { (_, horseId) -> horseId }
            )
    }

    private fun buildDeltaByPlayerId(transferEdges: List<TransferEdge>): Map<Int, Int> {
        val deltaByPlayerId = mutableMapOf<Int, Int>()
        transferEdges.forEach { edge ->
            deltaByPlayerId[edge.payerId] = (deltaByPlayerId[edge.payerId] ?: 0) - edge.amount
            deltaByPlayerId[edge.receiverId] = (deltaByPlayerId[edge.receiverId] ?: 0) + edge.amount
        }
        return deltaByPlayerId.filterValues { delta -> delta != 0 }
    }

    private fun updateLatestHorseBindingLocally(
        horseId: Int,
        boundOnTablePlayerId: Int?
    ) {
        latestAllPlayers = latestAllPlayers.map { player ->
            if (player.id == horseId && player.playerRole == PlayerRole.HORSE) {
                player.copy(boundOnTablePlayerId = boundOnTablePlayerId)
            } else {
                player
            }
        }
        boundHorseNamesByPlayerId = buildBoundHorseNamesByPlayerId(
            horses = selectActiveHorses(latestAllPlayers)
        )
    }

    private fun buildPlayerUiModels(players: List<Player>): List<PlayerCardUiModel> {
        return players.map { player ->
            val resolvedAvatarKey = PlayerAnimalAvatarCatalog.resolveAvatarKeyOrFallback(
                avatarKey = player.avatarKey,
                playerId = player.id,
                createdAt = player.createdAt
            )
            PlayerCardUiModel(
                id = player.id,
                name = player.name,
                avatarKey = resolvedAvatarKey,
                avatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(resolvedAvatarKey),
                roundDelta = formatDelta(roundDeltaByPlayerId[player.id] ?: 0),
                totalScore = formatScore(totalScoreByPlayerId[player.id] ?: player.score),
                isDealer = player.id == currentDealerPlayerId,
                status = statusByPlayerId[player.id] ?: PlayerStatus.ACTIVE,
                winOrder = winOrderByPlayerId[player.id],
                boundHorseNames = boundHorseNamesByPlayerId[player.id].orEmpty()
            )
        }
    }

    private fun buildBoundHorseNamesByPlayerId(horses: List<Player>): Map<Int, List<String>> {
        return horses
            .mapNotNull { horse ->
                val boundPlayerId = horse.boundOnTablePlayerId ?: return@mapNotNull null
                boundPlayerId to horse.name
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
    }

    private fun buildHorseUiModels(
        horses: List<Player>,
        activeOnTableById: Map<Int, Player>
    ): List<HorseUiModel> {
        return horses.map { horse ->
            val resolvedAvatarKey = PlayerAnimalAvatarCatalog.resolveAvatarKeyOrFallback(
                avatarKey = horse.avatarKey,
                playerId = horse.id,
                createdAt = horse.createdAt
            )
            HorseUiModel(
                id = horse.id,
                name = horse.name,
                avatarKey = resolvedAvatarKey,
                avatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(resolvedAvatarKey),
                boundOnTablePlayerName = horse.boundOnTablePlayerId?.let { targetId ->
                    activeOnTableById[targetId]?.name
                },
                roundDelta = formatDelta(roundDeltaByPlayerId[horse.id] ?: 0),
                totalScore = formatScore(totalScoreByPlayerId[horse.id] ?: horse.score)
            )
        }
    }

    private fun buildCurrentHorseUiModels(): List<HorseUiModel> {
        return buildHorseUiModels(
            horses = selectActiveHorses(latestAllPlayers),
            activeOnTableById = selectActiveOnTablePlayers(latestAllPlayers)
                .associateBy { player -> player.id }
        )
    }

    private fun formatDelta(delta: Int): String {
        val number = NumberFormat.getIntegerInstance().format(kotlin.math.abs(delta))
        return when {
            delta > 0 -> "+$number"
            delta < 0 -> "-$number"
            else -> "+0"
        }
    }

    private fun formatScore(score: Int): String {
        return NumberFormat.getIntegerInstance().format(score)
    }

    private fun playerNameOf(playerId: Int): String {
        return latestAllPlayers.firstOrNull { it.id == playerId }?.name ?: "P$playerId"
    }

    private fun buildScoringLiveLogs(
        actorId: Int,
        actionType: LiveLogActionType,
        fallbackTargetIds: List<Int>,
        deltaByPlayerId: Map<Int, Int>
    ): List<LiveLogItemUiModel> {
        val primaryLog = buildSingleLiveLog(
            actorId = actorId,
            actionType = actionType,
            fallbackTargetIds = fallbackTargetIds,
            deltaByPlayerId = deltaByPlayerId
        )
        val horseLogs = buildHorseLiveLogs(
            actionType = actionType,
            deltaByPlayerId = deltaByPlayerId,
            excludedActorId = actorId
        )
        return buildList {
            primaryLog?.let(::add)
            addAll(horseLogs)
        }
    }

    private fun buildHorseLiveLogs(
        actionType: LiveLogActionType,
        deltaByPlayerId: Map<Int, Int>,
        excludedActorId: Int
    ): List<LiveLogItemUiModel> {
        val activeHorses = selectActiveHorses(latestAllPlayers)
        return activeHorses.mapNotNull { horse ->
            val horseId = horse.id
            if (horseId == excludedActorId) {
                return@mapNotNull null
            }
            val horseDelta = deltaByPlayerId[horseId] ?: return@mapNotNull null
            // 只为收分侧马儿生成独立流水，扣分马儿不单独展示。
            if (horseDelta <= 0) {
                return@mapNotNull null
            }
            buildSingleLiveLog(
                actorId = horseId,
                actionType = actionType,
                fallbackTargetIds = emptyList(),
                deltaByPlayerId = deltaByPlayerId,
                becausePlayerName = horse.boundOnTablePlayerId?.let(::playerNameOf)
            )
        }
    }

    private fun buildSingleLiveLog(
        actorId: Int,
        actionType: LiveLogActionType,
        fallbackTargetIds: List<Int>,
        deltaByPlayerId: Map<Int, Int>,
        becausePlayerName: String? = null
    ): LiveLogItemUiModel? {
        val actorDelta = deltaByPlayerId[actorId] ?: return null
        val relatedPlayerDetails = scoringRelatedPlayerDetails(
            actorId = actorId,
            fallbackTargetIds = fallbackTargetIds,
            deltaByPlayerId = deltaByPlayerId
        )
        return LiveLogItemUiModel(
            id = nextLiveLogId++,
            actorName = playerNameOf(actorId),
            actorIsHorse = isHorsePlayer(actorId),
            becausePlayerName = becausePlayerName,
            actionType = actionType,
            relatedPlayerNames = relatedPlayerDetails.map { related -> related.name },
            relatedPlayerDetails = relatedPlayerDetails,
            amount = formatDelta(actorDelta),
            highlight = when {
                actorDelta > 0 -> LiveLogHighlight.POSITIVE
                actorDelta < 0 -> LiveLogHighlight.NEGATIVE
                else -> LiveLogHighlight.NEUTRAL
            }
        )
    }

    private fun scoringRelatedPlayerDetails(
        actorId: Int,
        fallbackTargetIds: List<Int>,
        deltaByPlayerId: Map<Int, Int>
    ): List<LiveLogRelatedPlayerUiModel> {
        val actorDelta = deltaByPlayerId[actorId] ?: 0
        val relatedIds = when {
            actorDelta > 0 -> deltaByPlayerId
                .filter { (playerId, delta) -> playerId != actorId && delta < 0 }
                .keys
                .toList()

            actorDelta < 0 -> deltaByPlayerId
                .filter { (playerId, delta) -> playerId != actorId && delta > 0 }
                .keys
                .toList()

            else -> fallbackTargetIds.filter { it != actorId }
        }
        val deltaByRelatedId = relatedIds.associateWith { playerId -> deltaByPlayerId[playerId] ?: 0 }
        return playerNamesWithDeltaById(deltaByRelatedId)
    }

    private fun playerNamesWithDeltaById(deltaByPlayerId: Map<Int, Int>): List<LiveLogRelatedPlayerUiModel> {
        if (deltaByPlayerId.isEmpty()) return emptyList()
        val orderedIds = orderedPlayerIds(deltaByPlayerId.keys)
        return orderedIds.map { playerId ->
            LiveLogRelatedPlayerUiModel(
                name = playerNameOf(playerId),
                delta = formatDelta(deltaByPlayerId[playerId] ?: 0)
            )
        }
    }

    private fun orderedPlayerIds(playerIds: Collection<Int>): List<Int> {
        if (playerIds.isEmpty()) return emptyList()
        val orderByPlayerId = latestAllPlayers
            .mapIndexed { index, player -> player.id to index }
            .toMap()
        return playerIds
            .distinct()
            .sortedWith(compareBy<Int> { playerId -> orderByPlayerId[playerId] ?: Int.MAX_VALUE }.thenBy { it })
    }

    private fun isHorsePlayer(playerId: Int): Boolean {
        return latestAllPlayers.firstOrNull { player -> player.id == playerId }?.playerRole == PlayerRole.HORSE
    }

    private fun huPlayerCount(): Int {
        return statusByPlayerId.values.count { it == PlayerStatus.HU || it == PlayerStatus.ZIMO }
    }

    private fun isHuCompleted(playerId: Int): Boolean {
        val status = statusByPlayerId[playerId] ?: PlayerStatus.ACTIVE
        return status == PlayerStatus.HU || status == PlayerStatus.ZIMO
    }

    private fun isPlayerActive(playerId: Int): Boolean {
        return statusByPlayerId[playerId] == PlayerStatus.ACTIVE
    }

    private fun canStartActionDraft(actorId: Int, action: BattleAction): Boolean {
        return when (action) {
            BattleAction.GANG -> canStartGangDraft(actorId)
            BattleAction.HU,
            BattleAction.ZIMO -> isPlayerActive(actorId)
        }
    }

    private fun canStartGangDraft(actorId: Int): Boolean {
        val status = statusByPlayerId[actorId] ?: return false
        if (status == PlayerStatus.ACTIVE) {
            return true
        }
        if (status != PlayerStatus.HU && status != PlayerStatus.ZIMO) {
            return false
        }
        return actorId == currentWinningPlayerId()
    }

    private fun canApplyScoringDraft(draft: EventDraftUiState): Boolean {
        return when (draft.action) {
            BattleAction.GANG -> canStartGangDraft(draft.actorId)
            BattleAction.HU,
            BattleAction.ZIMO -> isPlayerActive(draft.actorId)
        }
    }

    private fun actorStatusAfterGang(actorId: Int): PlayerStatus {
        return when (statusByPlayerId[actorId]) {
            PlayerStatus.HU -> PlayerStatus.HU
            PlayerStatus.ZIMO -> PlayerStatus.ZIMO
            else -> PlayerStatus.ACTIVE
        }
    }

    private fun currentWinningPlayerId(): Int? {
        return statusByPlayerId
            .filterValues { status -> status == PlayerStatus.HU || status == PlayerStatus.ZIMO }
            .keys
            .maxByOrNull { playerId -> winOrderByPlayerId[playerId] ?: Int.MIN_VALUE }
            ?.takeIf { playerId -> winOrderByPlayerId[playerId] != null }
    }

    private fun firstWinningPlayerIdOfCurrentRound(): Int? {
        val activePlayerIds = latestPlayers.map { player -> player.id }.toSet()
        return winOrderByPlayerId
            .asSequence()
            .filter { (playerId, _) -> playerId in activePlayerIds }
            .minByOrNull { (_, order) -> order }
            ?.key
    }

    private fun ensureCurrentDealerPlayerId(players: List<Player>) {
        val activePlayerIds = players.map { player -> player.id }.toSet()
        currentDealerPlayerId = currentDealerPlayerId?.takeIf { dealerId ->
            dealerId in activePlayerIds
        } ?: players.firstOrNull()?.id
    }

    private fun assignWinOrderIfNeeded(playerId: Int, status: PlayerStatus) {
        if (status != PlayerStatus.HU && status != PlayerStatus.ZIMO) {
            return
        }
        if (winOrderByPlayerId.containsKey(playerId)) {
            return
        }
        winOrderByPlayerId[playerId] = nextWinOrder++
    }

    private fun activeOpponentIds(actorId: Int): List<Int> {
        return latestPlayers
            .filter { player -> player.id != actorId && statusByPlayerId[player.id] == PlayerStatus.ACTIVE }
            .map { it.id }
    }

    private fun initialDraftStep(action: BattleAction): EventDraftStep {
        return when (action) {
            BattleAction.HU -> EventDraftStep.TARGET
            BattleAction.GANG -> EventDraftStep.GANG_TYPE
            BattleAction.ZIMO -> EventDraftStep.MULTIPLIER
        }
    }

    private fun normalizeMultiplier(multiplier: Int, maxMultiplier: Int): Int {
        val options = buildMultiplierOptions(maxMultiplier)
        val selected = options.lastOrNull { it <= multiplier }
        return selected ?: options.firstOrNull() ?: 1
    }

    private fun buildMultiplierOptions(maxMultiplier: Int): List<Int> {
        val limit = maxMultiplier.coerceAtLeast(1)
        val options = mutableListOf<Int>()
        var current = 1
        while (current <= limit) {
            options.add(current)
            if (current > Int.MAX_VALUE / 2) break
            current *= 2
        }
        return options
    }

    companion object {
        private const val BATTLE_PLAYER_LIMIT = 4
        private const val MAX_LOG_COUNT = 5
        private const val AUTO_SETTLE_HU_COUNT = 3
        private const val FIXED_GANG_MULTIPLIER = 1
    }
}

private data class PendingScoringAction(
    val actionType: BattleScoreActionType,
    val actorStatus: PlayerStatus,
    val targetIds: List<Int>
)

private data class TransferEdge(
    val payerId: Int,
    val receiverId: Int,
    val amount: Int
)

private fun PendingScoringAction.toLiveLogActionType(): LiveLogActionType {
    return when (actionType) {
        BattleScoreActionType.HU -> LiveLogActionType.HU
        BattleScoreActionType.ZIMO -> LiveLogActionType.ZIMO
        BattleScoreActionType.GANG_DIAN -> LiveLogActionType.GANG_DIAN
        BattleScoreActionType.GANG_BA -> LiveLogActionType.GANG_BA
        BattleScoreActionType.GANG_AN -> LiveLogActionType.GANG_AN
    }
}

private fun BattleScoreActionType.toPersistedEventType(): PersistedBattleEventType {
    return when (this) {
        BattleScoreActionType.HU -> PersistedBattleEventType.HU
        BattleScoreActionType.ZIMO -> PersistedBattleEventType.ZIMO
        BattleScoreActionType.GANG_DIAN -> PersistedBattleEventType.GANG_DIAN
        BattleScoreActionType.GANG_BA -> PersistedBattleEventType.GANG_BA
        BattleScoreActionType.GANG_AN -> PersistedBattleEventType.GANG_AN
    }
}

private data class RoundSnapshot(
    val totalScoreByPlayerId: Map<Int, Int>,
    val roundDeltaByPlayerId: Map<Int, Int>,
    val statusByPlayerId: Map<Int, PlayerStatus>,
    val winOrderByPlayerId: Map<Int, Int>,
    val roundScoringHistory: List<RoundScoringRecord>,
    val pendingRoundEvents: List<PendingRoundEvent>,
    val liveLogs: List<LiveLogItemUiModel>,
    val drawSettlementDraft: DrawSettlementDraftUiState?,
    val settlementPrompt: SettlementPromptUiState?,
    val nextLiveLogId: Int,
    val nextWinOrder: Int,
    val nextActionGroupId: Long
)

private data class RoundScoringRecord(
    val actionType: BattleScoreActionType,
    val actorId: Int,
    val deltaByPlayerId: Map<Int, Int>
)

private data class PendingRoundEvent(
    val actionGroupId: Long,
    val eventType: PersistedBattleEventType,
    val actorPlayerId: Int?,
    val multiplier: Int?,
    val payloadJson: String?,
    val deltaByPlayerId: Map<Int, Int>
)

private fun BattleScoreActionType.isGangType(): Boolean {
    return this == BattleScoreActionType.GANG_DIAN ||
        this == BattleScoreActionType.GANG_BA ||
        this == BattleScoreActionType.GANG_AN
}
