package com.ash.mahjong.feature.battle_score.vm

import com.ash.mahjong.data.player.Player
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.data.settings.GameSettings
import com.ash.mahjong.feature.battle_score.intent.BattleAction
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.intent.GangType
import com.ash.mahjong.feature.battle_score.state.DrawSettlementStep
import com.ash.mahjong.feature.battle_score.state.EventDraftStep
import com.ash.mahjong.feature.battle_score.state.LiveLogActionType
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.feature.battle_score.state.ResetAllConfirmStep
import com.ash.mahjong.feature.battle_score.state.SettlementPromptType
import com.ash.mahjong.test.fake.FakeGameSettingsRepository
import com.ash.mahjong.test.fake.FakeBattleRecordRepository
import com.ash.mahjong.test.fake.FakePlayerRepository
import com.ash.mahjong.test.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BattleScoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_observesActivePlayers_oldestFirstAndRequiresFourPlayers() = runTest {
        val repository = FakePlayerRepository(initialPlayers = fivePlayers())
        val viewModel = BattleScoreViewModel(
            playerRepository = repository,
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.players.size)
        assertEquals(listOf(1, 2, 3, 4), state.players.map { it.id })
        assertTrue(state.players.first().isDealer)
        assertEquals(false, state.requiresPlayerSetup)
        assertEquals(1..8, state.multiplierRange)
    }

    @Test
    fun homePlayers_onlyShowsActive_andRequiresSetupWhenActiveLessThanFour() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithThreeActive()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1, 3, 5), state.players.map { it.id })
        assertTrue(state.players.all { it.id == 1 || it.id == 3 || it.id == 5 })
        assertTrue(state.requiresPlayerSetup)
        assertFalse(state.canSettle)
    }

    @Test
    fun requiresSetup_whenTotalPlayersAtLeastFourButActiveLessThanFour() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithThreeActive()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.players.size)
        assertEquals(listOf(1, 3, 5), state.players.map { it.id })
        assertTrue(state.requiresPlayerSetup)
        assertFalse(state.canSettle)
    }

    @Test
    fun homePlayers_excludesActiveHorsePlayers() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithHorse()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1, 2, 4, 5), state.players.map { it.id })
        assertFalse(state.requiresPlayerSetup)
    }

    @Test
    fun horses_showsOnlyActiveHorsePlayers_sortedByCreatedAt() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithMixedHorseStatus()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(4, 6), state.horses.map { it.id })
    }

    @Test
    fun horses_resolvesBindingName_onlyWhenTargetIsActiveOnTable() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithHorseBindingCases()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        val horsesById = viewModel.uiState.value.horses.associateBy { it.id }
        assertEquals("A", horsesById.getValue(4).boundOnTablePlayerName)
        assertNull(horsesById.getValue(6).boundOnTablePlayerName)
        assertNull(horsesById.getValue(7).boundOnTablePlayerName)
        assertNull(horsesById.getValue(8).boundOnTablePlayerName)
    }

    @Test
    fun startHorseBinding_withActiveHorse_setsDraft() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithHorseBindingCases()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.StartHorseBinding(horseId = 4))
        advanceUntilIdle()

        val draft = viewModel.uiState.value.horseBindingDraft
        assertEquals(4, draft?.horseId)
        assertEquals("D", draft?.horseName)
    }

    @Test
    fun startHorseBinding_withInactiveOrNonHorse_isIgnored() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithInactiveHorse()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.StartHorseBinding(horseId = 1))
        viewModel.onIntent(BattleScoreIntent.StartHorseBinding(horseId = 5))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.horseBindingDraft)
    }

    @Test
    fun selectHorseBindingTarget_validTarget_updatesBindingAndClosesDraft() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithHorseBindingCases()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.StartHorseBinding(horseId = 7))
        viewModel.onIntent(BattleScoreIntent.SelectHorseBindingTarget(targetPlayerId = 1))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.horseBindingDraft)
        val horse = viewModel.uiState.value.horses.first { it.id == 7 }
        assertEquals("A", horse.boundOnTablePlayerName)
    }

    @Test
    fun selectHorseBindingTarget_targetOutsideHomePlayers_keepsDraftAndIgnoresUpdate() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithFiveOnTableAndHorse()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.StartHorseBinding(horseId = 6))
        viewModel.onIntent(BattleScoreIntent.SelectHorseBindingTarget(targetPlayerId = 5))
        advanceUntilIdle()

        val draft = viewModel.uiState.value.horseBindingDraft
        assertEquals(6, draft?.horseId)
        val horse = viewModel.uiState.value.horses.first { it.id == 6 }
        assertNull(horse.boundOnTablePlayerName)
    }

    @Test
    fun openPlayerSwapDialog_requiresHorseAndCanDismiss() = runTest {
        val viewModelWithHorse = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithBindingForNextRound()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()
        viewModelWithHorse.onIntent(BattleScoreIntent.OpenPlayerSwapDialog)
        advanceUntilIdle()
        assertTrue(viewModelWithHorse.uiState.value.playerSwapDialogVisible)

        viewModelWithHorse.onIntent(BattleScoreIntent.DismissPlayerSwapDialog)
        advanceUntilIdle()
        assertFalse(viewModelWithHorse.uiState.value.playerSwapDialogVisible)

        val viewModelWithoutHorse = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()
        viewModelWithoutHorse.onIntent(BattleScoreIntent.OpenPlayerSwapDialog)
        advanceUntilIdle()
        assertFalse(viewModelWithoutHorse.uiState.value.playerSwapDialogVisible)
    }

    @Test
    fun swapOnTableWithHorse_exchangesRolesAndKeepsFourOnTablePlayers() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithBindingForNextRound()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OpenPlayerSwapDialog)
        viewModel.onIntent(
            BattleScoreIntent.SwapOnTableWithHorse(
                onTablePlayerId = 1,
                horsePlayerId = 6
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.playerSwapDialogVisible)
        assertEquals(4, state.players.size)
        assertEquals(listOf(2, 3, 4, 6), state.players.map { it.id })
        assertEquals(listOf(1), state.horses.map { it.id })
        assertNull(state.horses.first().boundOnTablePlayerName)
    }

    @Test
    fun confirmSettleAndNextRound_clearsAllHorseBindings() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithBindingForNextRound()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()
        assertEquals(
            "A",
            viewModel.uiState.value.horses.first { it.id == 6 }.boundOnTablePlayerName
        )

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()
        viewModel.onIntent(BattleScoreIntent.ConfirmSettleAndNextRound)
        advanceUntilIdle()

        val horseAfterNextRound = viewModel.uiState.value.horses.first { it.id == 6 }
        assertNull(horseAfterNextRound.boundOnTablePlayerName)
    }

    @Test
    fun confirmSettleAndNextRound_immediatelyReflectsHorseBindingClearInUi() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithBindingForNextRound()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.ConfirmSettleAndNextRound)
        val horse = viewModel.uiState.value.horses.first { it.id == 6 }
        assertNull(horse.boundOnTablePlayerName)
    }

    @Test
    fun horseBinding_nonDrawAction_horseFollowsBoundPlayerDelta() = runTest {
        val repository = FakePlayerRepository(initialPlayers = playersWithScoringHorseBinding())
        val viewModel = BattleScoreViewModel(
            playerRepository = repository,
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        advanceUntilIdle()

        val horseAfterHu = viewModel.uiState.value.horses.first { it.id == 5 }
        assertEquals("+1", horseAfterHu.roundDelta)
        assertEquals("101", horseAfterHu.totalScore)

        repository.updatePlayerRole(playerId = 5, role = PlayerRole.ON_TABLE)
        repository.updatePlayerActiveStatus(playerId = 4, isActive = false)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("101", players.getValue(1).totalScore)
        assertEquals("98", players.getValue(2).totalScore)
        assertEquals("101", players.getValue(5).totalScore)
    }

    @Test
    fun liveLog_relatedPlayers_includesHorseWhenHorseActuallyPays() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithHorseBoundToTarget()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        advanceUntilIdle()

        val latestLog = viewModel.uiState.value.liveLogs.first()
        assertEquals(LiveLogActionType.HU, latestLog.actionType)
        assertEquals("+2", latestLog.amount)
        assertTrue(latestLog.relatedPlayerNames.contains("B"))
        assertTrue(latestLog.relatedPlayerNames.contains("E"))
    }

    @Test
    fun horseBinding_drawSettlement_horseDoesNotFollowBoundPlayerDelta() = runTest {
        val repository = FakePlayerRepository(initialPlayers = playersWithScoringHorseBinding())
        val viewModel = BattleScoreViewModel(
            playerRepository = repository,
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = true))
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingMultiplier(multiplier = 1))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        advanceUntilIdle()

        val horseAfterDraw = viewModel.uiState.value.horses.first { it.id == 5 }
        assertEquals("+0", horseAfterDraw.roundDelta)
        assertEquals("100", horseAfterDraw.totalScore)

        repository.updatePlayerRole(playerId = 5, role = PlayerRole.ON_TABLE)
        repository.updatePlayerActiveStatus(playerId = 4, isActive = false)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("99", players.getValue(1).totalScore)
        assertEquals("100", players.getValue(5).totalScore)
    }

    @Test
    fun lessThanFourPlayers_disablesDraftAndSettle() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(
                initialPlayers = listOf(
                    Player(id = 1, name = "A", score = 100, createdAt = 1L),
                    Player(id = 2, name = "B", score = 100, createdAt = 2L),
                    Player(id = 3, name = "C", score = 100, createdAt = 3L)
                )
            ),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.HU))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.requiresPlayerSetup)
        assertEquals(false, state.canSettle)
        assertNull(state.eventDraft)
    }

    @Test
    fun zimo_autoTargetsOnlyActivePlayersExceptActor() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository(
                initialSettings = GameSettings(basePoint = 1, cappingMultiplier = 8, hapticsEnabled = false)
            )
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 2, action = BattleAction.HU))
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 1))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 2))
        viewModel.onIntent(BattleScoreIntent.ConfirmEvent)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 3, action = BattleAction.ZIMO))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 3))
        viewModel.onIntent(BattleScoreIntent.ConfirmEvent)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("96", players.getValue(1).totalScore)
        assertEquals("102", players.getValue(2).totalScore)
        assertEquals("104", players.getValue(3).totalScore)
        assertEquals("98", players.getValue(4).totalScore)
        assertEquals(PlayerStatus.HU, players.getValue(2).status)
        assertEquals(PlayerStatus.ZIMO, players.getValue(3).status)
        assertEquals(1, players.getValue(2).winOrder)
        assertEquals(2, players.getValue(3).winOrder)

        val liveLogs = viewModel.uiState.value.liveLogs
        assertEquals("C", liveLogs.first().actorName)
        assertEquals(LiveLogActionType.ZIMO, liveLogs.first().actionType)
        assertEquals(listOf("A", "D"), liveLogs.first().relatedPlayerNames)
        assertEquals("B", liveLogs[1].actorName)
        assertEquals(LiveLogActionType.HU, liveLogs[1].actionType)
        assertEquals(listOf("A"), liveLogs[1].relatedPlayerNames)
    }

    @Test
    fun huAndZimo_assignWinOrderSequentially() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 3, action = BattleAction.ZIMO))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 1))
        viewModel.onIntent(BattleScoreIntent.ConfirmEvent)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals(1, players.getValue(1).winOrder)
        assertEquals(2, players.getValue(3).winOrder)
        assertEquals(3, players.getValue(2).winOrder)
        assertNull(players.getValue(4).winOrder)
    }

    @Test
    fun huPlayerCannotReceiveDuplicatedWinOrder() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.ZIMO))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 1))
        viewModel.onIntent(BattleScoreIntent.ConfirmEvent)
        advanceUntilIdle()

        hu(viewModel, actorId = 3, targetId = 4)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals(1, players.getValue(1).winOrder)
        assertEquals(2, players.getValue(3).winOrder)
        assertNull(players.getValue(2).winOrder)
    }

    @Test
    fun gang_dianRequiresTarget_baAndAnUseAllActiveOpponents() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.GANG))
        viewModel.onIntent(BattleScoreIntent.SelectGangType(GangType.DIAN))
        viewModel.onIntent(BattleScoreIntent.ConfirmDraftStep)
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 2))
        advanceUntilIdle()

        val afterDian = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("102", afterDian.getValue(1).totalScore)
        assertEquals("98", afterDian.getValue(2).totalScore)
        assertEquals(LiveLogActionType.GANG_DIAN, viewModel.uiState.value.liveLogs.first().actionType)
        assertEquals("A", viewModel.uiState.value.liveLogs.first().actorName)
        assertEquals(listOf("B"), viewModel.uiState.value.liveLogs.first().relatedPlayerNames)

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.GANG))
        viewModel.onIntent(BattleScoreIntent.SelectGangType(GangType.BA))
        viewModel.onIntent(BattleScoreIntent.ConfirmDraftStep)
        advanceUntilIdle()

        val afterBa = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("105", afterBa.getValue(1).totalScore)
        assertEquals("97", afterBa.getValue(2).totalScore)
        assertEquals("99", afterBa.getValue(3).totalScore)
        assertEquals("99", afterBa.getValue(4).totalScore)
        assertEquals(LiveLogActionType.GANG_BA, viewModel.uiState.value.liveLogs.first().actionType)
        assertEquals("A", viewModel.uiState.value.liveLogs.first().actorName)
        assertEquals(listOf("B", "C", "D"), viewModel.uiState.value.liveLogs.first().relatedPlayerNames)
    }

    @Test
    fun gangBaOrAn_confirmDraftStep_settlesImmediatelyWithoutMultiplierStep() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.GANG))
        viewModel.onIntent(BattleScoreIntent.SelectGangType(GangType.BA))
        viewModel.onIntent(BattleScoreIntent.ConfirmDraftStep)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.eventDraft)
        assertEquals(
            LiveLogActionType.GANG_BA,
            viewModel.uiState.value.liveLogs.first().actionType
        )

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.GANG))
        viewModel.onIntent(BattleScoreIntent.SelectGangType(GangType.AN))
        viewModel.onIntent(BattleScoreIntent.ConfirmDraftStep)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.eventDraft)
        assertEquals(
            LiveLogActionType.GANG_AN,
            viewModel.uiState.value.liveLogs.first().actionType
        )
    }

    @Test
    fun gangDian_selectTarget_settlesImmediatelyWithoutMultiplierStep() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.GANG))
        viewModel.onIntent(BattleScoreIntent.SelectGangType(GangType.DIAN))
        viewModel.onIntent(BattleScoreIntent.ConfirmDraftStep)
        advanceUntilIdle()

        assertEquals(EventDraftStep.TARGET, viewModel.uiState.value.eventDraft?.step)

        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 2))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.eventDraft)
        assertEquals(
            LiveLogActionType.GANG_DIAN,
            viewModel.uiState.value.liveLogs.first().actionType
        )
    }

    @Test
    fun huAndZimo_stillRequireMultiplierStep() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.HU))
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 2))
        advanceUntilIdle()
        assertEquals(EventDraftStep.MULTIPLIER, viewModel.uiState.value.eventDraft?.step)

        viewModel.onIntent(BattleScoreIntent.CancelEventDraft)
        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.ZIMO))
        advanceUntilIdle()
        assertEquals(EventDraftStep.MULTIPLIER, viewModel.uiState.value.eventDraft?.step)
    }

    @Test
    fun threeHu_triggersAutoSettlementConfirm_andNextRoundResetsRoundState() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        val promptedState = viewModel.uiState.value
        assertNotNull(promptedState.settlementPrompt)
        assertEquals(
            SettlementPromptType.AUTO_THREE_HU,
            promptedState.settlementPrompt?.type
        )

        viewModel.onIntent(BattleScoreIntent.ConfirmSettleAndNextRound)
        advanceUntilIdle()

        val settledState = viewModel.uiState.value
        assertEquals(2, settledState.currentRound)
        assertTrue(settledState.liveLogs.isEmpty())
        assertTrue(settledState.players.all { it.status == PlayerStatus.ACTIVE })
        assertTrue(settledState.players.all { it.roundDelta == "+0" })
        assertTrue(settledState.players.all { it.winOrder == null })
    }

    @Test
    fun manualSettleClick_withThreeHu_showsAutoSettlementPrompt() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.DismissSettlementPrompt)
        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        advanceUntilIdle()

        assertEquals(
            SettlementPromptType.AUTO_THREE_HU,
            viewModel.uiState.value.settlementPrompt?.type
        )
    }

    @Test
    fun manualSettleClick_withoutThreeHu_startsDrawSettlementDraft() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        advanceUntilIdle()

        val draft = viewModel.uiState.value.drawSettlementDraft
        assertNotNull(draft)
        assertEquals(4, draft?.orderedPendingPlayerIds?.size)
        assertEquals(
            DrawSettlementStep.CHOOSE_TING,
            draft?.step
        )
        assertNull(viewModel.uiState.value.settlementPrompt)
    }

    @Test
    fun drawSettlement_mixedTingChoices_appliesCompensationAndShowsConfirmPrompt() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = true))
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingMultiplier(multiplier = 2))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = true))
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingMultiplier(multiplier = 1))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("104", players.getValue(1).totalScore)
        assertEquals("97", players.getValue(2).totalScore)
        assertEquals("102", players.getValue(3).totalScore)
        assertEquals("97", players.getValue(4).totalScore)
        assertEquals(
            SettlementPromptType.DRAW_RESULT_CONFIRM,
            viewModel.uiState.value.settlementPrompt?.type
        )
        assertNull(viewModel.uiState.value.drawSettlementDraft)
        assertEquals(LiveLogActionType.DRAW_SETTLEMENT, viewModel.uiState.value.liveLogs.first().actionType)
    }

    @Test
    fun drawSettlementDraft_orderSkipsHuPlayersInSeatOrder() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 4, targetId = 3)
        hu(viewModel, actorId = 2, targetId = 1)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        advanceUntilIdle()

        val draft = viewModel.uiState.value.drawSettlementDraft
        assertNotNull(draft)
        assertEquals(
            listOf(1, 3),
            draft?.orderedPendingPlayerIds
        )
        assertEquals(DrawSettlementStep.CHOOSE_TING, draft?.step)
    }

    @Test
    fun drawSettlement_noTingOrNoNonTing_keepsScoresUnchanged() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        repeat(4) {
            viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
            viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        }
        advanceUntilIdle()

        val playersAfterNoTing = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("100", playersAfterNoTing.getValue(1).totalScore)
        assertEquals("100", playersAfterNoTing.getValue(2).totalScore)
        assertEquals("100", playersAfterNoTing.getValue(3).totalScore)
        assertEquals("100", playersAfterNoTing.getValue(4).totalScore)

        viewModel.onIntent(BattleScoreIntent.DismissSettlementPrompt)
        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        repeat(4) {
            viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = true))
            viewModel.onIntent(BattleScoreIntent.SelectDrawTingMultiplier(multiplier = 1))
            viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        }
        advanceUntilIdle()

        val playersAfterAllTing = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("100", playersAfterAllTing.getValue(1).totalScore)
        assertEquals("100", playersAfterAllTing.getValue(2).totalScore)
        assertEquals("100", playersAfterAllTing.getValue(3).totalScore)
        assertEquals("100", playersAfterAllTing.getValue(4).totalScore)
    }

    @Test
    fun drawSettlement_nonTingGangActor_refundsGangIncludingHorseFollow() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = playersWithScoringHorseBinding()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.GANG))
        viewModel.onIntent(BattleScoreIntent.SelectGangType(GangType.BA))
        viewModel.onIntent(BattleScoreIntent.ConfirmDraftStep)
        advanceUntilIdle()

        val playersAfterGang = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("103", playersAfterGang.getValue(1).totalScore)
        assertEquals("98", playersAfterGang.getValue(2).totalScore)
        assertEquals("98", playersAfterGang.getValue(3).totalScore)
        assertEquals("98", playersAfterGang.getValue(4).totalScore)
        val horseAfterGang = viewModel.uiState.value.horses.first { it.id == 5 }
        assertEquals("103", horseAfterGang.totalScore)

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        repeat(4) {
            viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
            viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        }
        advanceUntilIdle()

        val playersAfterDraw = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals("100", playersAfterDraw.getValue(1).totalScore)
        assertEquals("100", playersAfterDraw.getValue(2).totalScore)
        assertEquals("100", playersAfterDraw.getValue(3).totalScore)
        assertEquals("100", playersAfterDraw.getValue(4).totalScore)
        val horseAfterDraw = viewModel.uiState.value.horses.first { it.id == 5 }
        assertEquals("100", horseAfterDraw.totalScore)
        assertTrue(
            viewModel.uiState.value.liveLogs.any { log ->
                log.actionType == LiveLogActionType.GANG_REFUND
            }
        )
    }

    @Test
    fun drawSettlement_confirmPrompt_thenConfirmNextRound_resetsRoundState() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OnFabClick)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = true))
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingMultiplier(multiplier = 2))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        viewModel.onIntent(BattleScoreIntent.SelectDrawTingChoice(isTing = false))
        viewModel.onIntent(BattleScoreIntent.ConfirmDrawSettlementSelection)
        advanceUntilIdle()

        assertEquals(
            SettlementPromptType.DRAW_RESULT_CONFIRM,
            viewModel.uiState.value.settlementPrompt?.type
        )
        assertTrue(viewModel.uiState.value.players.any { it.roundDelta != "+0" })

        viewModel.onIntent(BattleScoreIntent.ConfirmSettleAndNextRound)
        advanceUntilIdle()

        val settledState = viewModel.uiState.value
        assertEquals(2, settledState.currentRound)
        assertNull(settledState.drawSettlementDraft)
        assertNull(settledState.settlementPrompt)
        assertTrue(settledState.liveLogs.isEmpty())
        assertTrue(settledState.players.all { it.status == PlayerStatus.ACTIVE })
        assertTrue(settledState.players.all { it.roundDelta == "+0" })
    }

    @Test
    fun undoLastEvent_rollsBackScoresAndStatuses() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        advanceUntilIdle()
        viewModel.onIntent(BattleScoreIntent.UndoLastEvent)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val players = state.players.associateBy { it.id }
        assertEquals("100", players.getValue(1).totalScore)
        assertEquals("100", players.getValue(2).totalScore)
        assertEquals(PlayerStatus.ACTIVE, players.getValue(1).status)
        assertEquals(PlayerStatus.ACTIVE, players.getValue(2).status)
        assertNull(players.getValue(1).winOrder)
        assertNull(players.getValue(2).winOrder)
        assertEquals(false, state.canUndo)
        assertTrue(state.liveLogs.isEmpty())
    }

    @Test
    fun resetAll_requiresTwoConfirmSteps() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.OpenResetAllConfirmDialog)
        assertEquals(ResetAllConfirmStep.FIRST, viewModel.uiState.value.resetAllConfirmStep)

        viewModel.onIntent(BattleScoreIntent.ConfirmResetAllConfirmDialog)
        assertEquals(ResetAllConfirmStep.SECOND, viewModel.uiState.value.resetAllConfirmStep)

        viewModel.onIntent(BattleScoreIntent.DismissResetAllConfirmDialog)
        assertNull(viewModel.uiState.value.resetAllConfirmStep)
    }

    @Test
    fun resetAll_afterSecondConfirm_resetsRoundAndScores() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.players.any { it.totalScore != "100" })
        assertNotNull(viewModel.uiState.value.settlementPrompt)
        assertEquals(1, viewModel.uiState.value.currentRound)

        viewModel.onIntent(BattleScoreIntent.OpenResetAllConfirmDialog)
        viewModel.onIntent(BattleScoreIntent.ConfirmResetAllConfirmDialog)
        viewModel.onIntent(BattleScoreIntent.ConfirmResetAllConfirmDialog)
        advanceUntilIdle()

        val resetState = viewModel.uiState.value
        assertEquals(1, resetState.currentRound)
        assertTrue(resetState.players.all { it.totalScore == "100" })
        assertTrue(resetState.players.all { it.roundDelta == "+0" })
        assertTrue(resetState.players.all { it.status == PlayerStatus.ACTIVE })
        assertTrue(resetState.liveLogs.isEmpty())
        assertFalse(resetState.canUndo)
        assertNull(resetState.settlementPrompt)
        assertNull(resetState.resetAllConfirmStep)
    }

    @Test
    fun undoLastEvent_restoresWinOrderAndCounter() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.UndoLastEvent)
        advanceUntilIdle()
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals(1, players.getValue(1).winOrder)
        assertEquals(2, players.getValue(2).winOrder)
        assertNull(players.getValue(3).winOrder)
    }

    @Test
    fun nextRound_resetsWinOrderAndRestartsFromOne() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.ConfirmSettleAndNextRound)
        hu(viewModel, actorId = 4, targetId = 1)
        advanceUntilIdle()

        val players = viewModel.uiState.value.players.associateBy { it.id }
        assertEquals(1, players.getValue(4).winOrder)
        assertNull(players.getValue(1).winOrder)
        assertNull(players.getValue(2).winOrder)
        assertNull(players.getValue(3).winOrder)
    }

    @Test
    fun settingsUpdate_updatesMultiplierRangeAndClampsDraft() = runTest {
        val settingsRepository = FakeGameSettingsRepository(
            initialSettings = GameSettings(basePoint = 1, cappingMultiplier = 8, hapticsEnabled = false)
        )
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = settingsRepository
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.HU))
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 2))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 8))
        advanceUntilIdle()

        settingsRepository.updateCappingMultiplier(3)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1..3, state.multiplierRange)
        assertEquals(2, state.eventDraft?.multiplier)
    }

    @Test
    fun cancelFromMultiplier_huReturnsToTargetStep() = runTest {
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository()
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.HU))
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 2))
        advanceUntilIdle()

        assertEquals(EventDraftStep.MULTIPLIER, viewModel.uiState.value.eventDraft?.step)
        viewModel.onIntent(BattleScoreIntent.BackEventDraftStep)
        advanceUntilIdle()

        val afterBack = viewModel.uiState.value.eventDraft
        assertEquals(EventDraftStep.TARGET, afterBack?.step)
    }

    @Test
    fun multiplier_usesPowersOfTwoUntilCappingLimit() = runTest {
        val settingsRepository = FakeGameSettingsRepository(
            initialSettings = GameSettings(basePoint = 1, cappingMultiplier = 20, hapticsEnabled = false)
        )
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = settingsRepository
        )
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = 1, action = BattleAction.HU))
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = 2))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 16))
        advanceUntilIdle()

        assertEquals(16, viewModel.uiState.value.eventDraft?.multiplier)

        settingsRepository.updateCappingMultiplier(12)
        advanceUntilIdle()

        assertEquals(8, viewModel.uiState.value.eventDraft?.multiplier)
    }

    @Test
    fun roundEvents_areNotPersistedBeforeSettlementConfirmation() = runTest {
        val battleRecordRepository = FakeBattleRecordRepository()
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository(),
            battleRecordRepository = battleRecordRepository
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        advanceUntilIdle()

        assertTrue(battleRecordRepository.persistedRounds.isEmpty())
    }

    @Test
    fun confirmSettleAndNextRound_persistsCurrentRoundAsBatch() = runTest {
        val battleRecordRepository = FakeBattleRecordRepository()
        val viewModel = BattleScoreViewModel(
            playerRepository = FakePlayerRepository(initialPlayers = fourPlayers()),
            gameSettingsRepository = FakeGameSettingsRepository(),
            battleRecordRepository = battleRecordRepository
        )
        advanceUntilIdle()

        hu(viewModel, actorId = 1, targetId = 2)
        hu(viewModel, actorId = 3, targetId = 4)
        hu(viewModel, actorId = 2, targetId = 4)
        advanceUntilIdle()

        viewModel.onIntent(BattleScoreIntent.ConfirmSettleAndNextRound)
        advanceUntilIdle()

        assertEquals(1, battleRecordRepository.persistedRounds.size)
        val persistedRound = battleRecordRepository.persistedRounds.single()
        assertEquals(1, persistedRound.roundNo)
        assertTrue(persistedRound.events.isNotEmpty())
        assertTrue(
            persistedRound.events.any { event ->
                event.eventType.name == "HU"
            }
        )
    }

    private fun hu(viewModel: BattleScoreViewModel, actorId: Int, targetId: Int) {
        viewModel.onIntent(BattleScoreIntent.SelectAction(actorId = actorId, action = BattleAction.HU))
        viewModel.onIntent(BattleScoreIntent.SelectTarget(targetId = targetId))
        viewModel.onIntent(BattleScoreIntent.SelectMultiplier(multiplier = 1))
        viewModel.onIntent(BattleScoreIntent.ConfirmEvent)
    }

    private fun fourPlayers(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L),
        Player(id = 2, name = "B", score = 100, createdAt = 2L),
        Player(id = 3, name = "C", score = 100, createdAt = 3L),
        Player(id = 4, name = "D", score = 100, createdAt = 4L)
    )

    private fun fivePlayers(): List<Player> = fourPlayers() + Player(
        id = 5,
        name = "E",
        score = 100,
        createdAt = 5L
    )

    private fun playersWithThreeActive(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = false),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = false),
        Player(id = 5, name = "E", score = 100, createdAt = 5L, isActive = true)
    )

    private fun playersWithHorse(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.HORSE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true),
        Player(id = 5, name = "E", score = 100, createdAt = 5L, isActive = true)
    )

    private fun playersWithMixedHorseStatus(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = false, playerRole = PlayerRole.HORSE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true, playerRole = PlayerRole.HORSE),
        Player(id = 5, name = "E", score = 100, createdAt = 5L, isActive = true),
        Player(id = 6, name = "F", score = 100, createdAt = 6L, isActive = true, playerRole = PlayerRole.HORSE)
    )

    private fun playersWithHorseBindingCases(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = false, playerRole = PlayerRole.ON_TABLE),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.HORSE),
        Player(
            id = 4,
            name = "D",
            score = 100,
            createdAt = 4L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 1
        ),
        Player(
            id = 6,
            name = "F",
            score = 100,
            createdAt = 6L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 2
        ),
        Player(
            id = 7,
            name = "G",
            score = 100,
            createdAt = 7L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 3
        ),
        Player(
            id = 8,
            name = "H",
            score = 100,
            createdAt = 8L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 999
        )
    )

    private fun playersWithInactiveHorse(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 5, name = "E", score = 100, createdAt = 5L, isActive = false, playerRole = PlayerRole.HORSE)
    )

    private fun playersWithFiveOnTableAndHorse(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 5, name = "E", score = 100, createdAt = 5L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 6, name = "F", score = 100, createdAt = 6L, isActive = true, playerRole = PlayerRole.HORSE)
    )

    private fun playersWithBindingForNextRound(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(
            id = 6,
            name = "F",
            score = 100,
            createdAt = 6L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 1
        )
    )

    private fun playersWithScoringHorseBinding(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(
            id = 5,
            name = "E",
            score = 100,
            createdAt = 5L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 1
        )
    )

    private fun playersWithHorseBoundToTarget(): List<Player> = listOf(
        Player(id = 1, name = "A", score = 100, createdAt = 1L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 2, name = "B", score = 100, createdAt = 2L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 3, name = "C", score = 100, createdAt = 3L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(id = 4, name = "D", score = 100, createdAt = 4L, isActive = true, playerRole = PlayerRole.ON_TABLE),
        Player(
            id = 5,
            name = "E",
            score = 100,
            createdAt = 5L,
            isActive = true,
            playerRole = PlayerRole.HORSE,
            boundOnTablePlayerId = 2
        )
    )
}
