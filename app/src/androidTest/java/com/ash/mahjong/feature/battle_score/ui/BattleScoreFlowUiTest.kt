package com.ash.mahjong.feature.battle_score.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.intent.BattleAction
import com.ash.mahjong.feature.battle_score.intent.BattleScoreIntent
import com.ash.mahjong.feature.battle_score.intent.GangType
import com.ash.mahjong.feature.battle_score.state.BattleScoreUiState
import com.ash.mahjong.feature.battle_score.state.DrawSettlementDraftUiState
import com.ash.mahjong.feature.battle_score.state.DrawSettlementStep
import com.ash.mahjong.feature.battle_score.state.EventDraftStep
import com.ash.mahjong.feature.battle_score.state.EventDraftUiState
import com.ash.mahjong.feature.battle_score.state.HorseBindingDraftUiState
import com.ash.mahjong.feature.battle_score.state.HorseUiModel
import com.ash.mahjong.feature.battle_score.state.LiveLogActionType
import com.ash.mahjong.feature.battle_score.state.LiveLogHighlight
import com.ash.mahjong.feature.battle_score.state.LiveLogItemUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerCardUiModel
import com.ash.mahjong.feature.battle_score.state.PlayerStatus
import com.ash.mahjong.feature.battle_score.state.SettlementPromptType
import com.ash.mahjong.feature.battle_score.state.SettlementPromptUiState
import com.ash.mahjong.ui.theme.MahjongTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class BattleScoreFlowUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun zimoDraft_showsMultiplierDialog() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        eventDraft = EventDraftUiState(
                            step = EventDraftStep.MULTIPLIER,
                            action = BattleAction.ZIMO,
                            actorId = 1,
                            gangType = null,
                            targetId = null,
                            multiplier = 2
                        )
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithTag(BattleScoreTestTags.MULTIPLIER_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(R.string.battle_draft_multiplier_dialog_title, 0, 3)
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText(
            context.getString(R.string.battle_draft_target_hu)
        ).assertCountEquals(0)
    }

    @Test
    fun gangDraft_doesNotShowMultiplierDialog() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        eventDraft = EventDraftUiState(
                            step = EventDraftStep.MULTIPLIER,
                            action = BattleAction.GANG,
                            actorId = 1,
                            gangType = GangType.BA,
                            targetId = null,
                            multiplier = null
                        )
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onAllNodesWithTag(BattleScoreTestTags.MULTIPLIER_DIALOG).assertCountEquals(0)
    }

    @Test
    fun requiresSetup_showsGuideCard() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        isPlayersLoaded = true,
                        requiresPlayerSetup = true,
                        canSettle = false
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(context.getString(R.string.battle_setup_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.battle_setup_action)).assertIsDisplayed()
    }

    @Test
    fun requiresSetup_beforePlayersLoaded_showsSkeletonAndHidesGuideCard() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        isPlayersLoaded = false,
                        requiresPlayerSetup = true,
                        canSettle = false
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithTag(BattleScoreTestTags.LOADING_SKELETON).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.battle_setup_title)).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.battle_setup_action)).assertCountEquals(0)
        composeRule.onAllNodesWithTag(BattleScoreTestTags.PLAYERS_GRID).assertCountEquals(0)
    }

    @Test
    fun autoSettlementPrompt_showsDialog() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        settlementPrompt = SettlementPromptUiState(SettlementPromptType.AUTO_THREE_HU)
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(context.getString(R.string.battle_settlement_title)).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(R.string.battle_settlement_next_round)
        ).assertIsDisplayed()
    }

    @Test
    fun drawResultConfirm_clickNextRound_dispatchesConfirmIntent() {
        val intents = mutableListOf<BattleScoreIntent>()
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        settlementPrompt = SettlementPromptUiState(SettlementPromptType.DRAW_RESULT_CONFIRM)
                    ),
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(
            context.getString(R.string.battle_settlement_next_round)
        ).performClick()

        assertTrue(intents.contains(BattleScoreIntent.ConfirmSettleAndNextRound))
    }

    @Test
    fun drawSettlementDraft_tingStep_showsChoiceDialog() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        drawSettlementDraft = DrawSettlementDraftUiState(
                            orderedPendingPlayerIds = listOf(1, 2, 4),
                            currentIndex = 0,
                            choicesByPlayerId = emptyMap(),
                            step = DrawSettlementStep.CHOOSE_TING,
                            currentTingChoice = null,
                            currentMultiplier = null
                        )
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithTag(BattleScoreTestTags.DRAW_TING_CHOICE_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(R.string.battle_draw_choice_ting)
        ).assertIsDisplayed()
    }

    @Test
    fun drawSettlementDraft_multiplierStep_showsOptions() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        drawSettlementDraft = DrawSettlementDraftUiState(
                            orderedPendingPlayerIds = listOf(1, 2, 4),
                            currentIndex = 1,
                            choicesByPlayerId = mapOf(
                                1 to com.ash.mahjong.feature.battle_score.state.DrawSettlementChoiceUiState(
                                    isTing = true,
                                    multiplier = 2
                                )
                            ),
                            step = DrawSettlementStep.CHOOSE_MULTIPLIER,
                            currentTingChoice = true,
                            currentMultiplier = 2
                        )
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.DRAW_MULTIPLIER_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.drawMultiplierOption(2)).assertIsDisplayed()
    }

    @Test
    fun liveLog_rendersActorAndFullRelatedNames() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(
                        liveLogs = listOf(
                            LiveLogItemUiModel(
                                id = 1,
                                actorName = "王大",
                                actionType = LiveLogActionType.ZIMO,
                                relatedPlayerNames = listOf("王二", "李三", "赵四"),
                                amount = "+6",
                                highlight = LiveLogHighlight.POSITIVE
                            )
                        )
                    ),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val title = context.getString(
            R.string.battle_log_title_template,
            "王大",
            context.getString(R.string.battle_action_zimo)
        )
        val relatedNames = listOf("王二", "李三", "赵四")
            .joinToString(context.getString(R.string.battle_log_name_separator))
        val subtitle = context.getString(R.string.battle_log_related_zimo_template, relatedNames)

        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun horsesExist_showsHorseSectionBelowTopBar_andAboveScrollableContent() {
        val state = baseState(
            horses = listOf(
                HorseUiModel(
                    id = 6,
                    name = "马儿A",
                    avatarKey = "horse_6",
                    avatarEmoji = "H6",
                    boundOnTablePlayerName = "A",
                    roundDelta = "+0",
                    totalScore = "100"
                )
            )
        )
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val topBarBounds = composeRule.onNodeWithTag(BattleScoreTestTags.TOP_BAR).fetchSemanticsNode().boundsInRoot
        val playersBounds = composeRule.onNodeWithTag(BattleScoreTestTags.PLAYERS_GRID).fetchSemanticsNode().boundsInRoot
        val horsesBounds = composeRule.onNodeWithTag(BattleScoreTestTags.HORSE_SECTION).fetchSemanticsNode().boundsInRoot
        val liveLogBounds = composeRule.onNodeWithTag(BattleScoreTestTags.LIVE_LOG_SECTION).fetchSemanticsNode().boundsInRoot

        assertTrue(horsesBounds.top >= topBarBounds.bottom)
        assertTrue(playersBounds.top >= horsesBounds.bottom)
        assertTrue(liveLogBounds.top > playersBounds.top)
    }

    @Test
    fun horsesEmpty_hidesHorseSection() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(horses = emptyList()),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onAllNodesWithTag(BattleScoreTestTags.HORSE_SECTION).assertCountEquals(0)
    }

    @Test
    fun horseSection_textComesFromResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val state = baseState(
            horses = listOf(
                HorseUiModel(
                    id = 6,
                    name = "马儿A",
                    avatarKey = "horse_6",
                    avatarEmoji = "H6",
                    boundOnTablePlayerName = "A",
                    roundDelta = "+0",
                    totalScore = "100"
                ),
                HorseUiModel(
                    id = 7,
                    name = "马儿B",
                    avatarKey = "horse_7",
                    avatarEmoji = "H7",
                    boundOnTablePlayerName = null,
                    roundDelta = "+0",
                    totalScore = "100"
                )
            )
        )
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.battle_horse_section_title)).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(R.string.battle_horse_item_bound_template, "马儿A", "A")
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(R.string.battle_horse_item_idle_template, "马儿B")
        ).assertIsDisplayed()
    }

    @Test
    fun horseCard_click_dispatchesStartBindingIntent() {
        val intents = mutableListOf<BattleScoreIntent>()
        val state = baseState(
            horses = listOf(
                HorseUiModel(
                    id = 6,
                    name = "马儿A",
                    avatarKey = "horse_6",
                    avatarEmoji = "H6",
                    boundOnTablePlayerName = null,
                    roundDelta = "+0",
                    totalScore = "100"
                )
            )
        )
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.horseCard(6)).performClick()
        assertTrue(intents.contains(BattleScoreIntent.StartHorseBinding(horseId = 6)))
    }

    @Test
    fun horseBindingDraft_showsDialogAndSelectTarget_dispatchesIntent() {
        val intents = mutableListOf<BattleScoreIntent>()
        val state = baseState(
            horses = listOf(
                HorseUiModel(
                    id = 6,
                    name = "马儿A",
                    avatarKey = "horse_6",
                    avatarEmoji = "H6",
                    boundOnTablePlayerName = null,
                    roundDelta = "+0",
                    totalScore = "100"
                )
            ),
            horseBindingDraft = HorseBindingDraftUiState(horseId = 6, horseName = "马儿A")
        )
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.HORSE_BINDING_DIALOG).assertIsDisplayed()
        composeRule.onNodeWithTag(BattleScoreTestTags.horseBindingTarget(1)).performClick()
        assertTrue(intents.contains(BattleScoreIntent.SelectHorseBindingTarget(targetPlayerId = 1)))
    }

    @Test
    fun huOrZimoPlayer_showsWinOrderBadgeAndStatusText() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithTag(BattleScoreTestTags.winOrderBadge(3)).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.getString(R.string.player_status_hu)
        ).assertIsDisplayed()
    }

    @Test
    fun swapPlayersButton_clickDispatchesOpenDialogIntent_whenEnabled() {
        val intents = mutableListOf<BattleScoreIntent>()
        val state = baseState(
            horses = listOf(
                HorseUiModel(
                    id = 6,
                    name = "马儿A",
                    avatarKey = "horse_6",
                    avatarEmoji = "H6",
                    boundOnTablePlayerName = null,
                    roundDelta = "+0",
                    totalScore = "100"
                )
            )
        )
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.SWAP_PLAYERS_BUTTON).performClick()
        assertTrue(intents.contains(BattleScoreIntent.OpenPlayerSwapDialog))
    }

    @Test
    fun swapPlayersButton_isDisabledButVisible_whenCannotSwap() {
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(horses = emptyList()),
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.SWAP_PLAYERS_BUTTON)
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun activePlayer_doesNotShowWinOrderBadge() {
        val state = baseState().copy(
            players = baseState().players.map { player ->
                player.copy(status = PlayerStatus.ACTIVE, winOrder = null)
            }
        )

        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = {},
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onAllNodesWithTag(BattleScoreTestTags.winOrderBadge(3)).assertCountEquals(0)
    }

    @Test
    fun huPlayer_gangButtonStillEnabled_dispatchesGangIntent() {
        val intents = mutableListOf<BattleScoreIntent>()
        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = baseState(),
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.gangButton(3)).performClick()
        assertTrue(
            intents.contains(
                BattleScoreIntent.SelectAction(
                    actorId = 3,
                    action = BattleAction.GANG
                )
            )
        )
    }

    @Test
    fun multipleHuOrZimo_onlyLatestWinnerGangButtonEnabled() {
        val intents = mutableListOf<BattleScoreIntent>()
        val state = baseState().copy(
            players = baseState().players.map { player ->
                when (player.id) {
                    1 -> player.copy(status = PlayerStatus.HU, winOrder = 1)
                    3 -> player.copy(status = PlayerStatus.ZIMO, winOrder = 2)
                    else -> player.copy(status = PlayerStatus.ACTIVE, winOrder = null)
                }
            }
        )

        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.gangButton(1)).assertIsNotEnabled()
        composeRule.onNodeWithTag(BattleScoreTestTags.gangButton(3)).performClick()
        assertTrue(
            intents.contains(
                BattleScoreIntent.SelectAction(
                    actorId = 3,
                    action = BattleAction.GANG
                )
            )
        )
    }

    @Test
    fun activePlayer_gangButtonRemainsEnabled_withMultipleHuOrZimoPlayers() {
        val intents = mutableListOf<BattleScoreIntent>()
        val state = baseState().copy(
            players = baseState().players.map { player ->
                when (player.id) {
                    1 -> player.copy(status = PlayerStatus.HU, winOrder = 1)
                    3 -> player.copy(status = PlayerStatus.ZIMO, winOrder = 2)
                    else -> player.copy(status = PlayerStatus.ACTIVE, winOrder = null)
                }
            }
        )

        composeRule.setContent {
            MahjongTheme {
                BattleScoreScreen(
                    uiState = state,
                    onIntent = { intents.add(it) },
                    onGoToPlayers = {}
                )
            }
        }

        composeRule.onNodeWithTag(BattleScoreTestTags.gangButton(2)).performClick()
        assertTrue(
            intents.contains(
                BattleScoreIntent.SelectAction(
                    actorId = 2,
                    action = BattleAction.GANG
                )
            )
        )
    }

    private fun baseState(
        isPlayersLoaded: Boolean = true,
        requiresPlayerSetup: Boolean = false,
        canSettle: Boolean = true,
        eventDraft: EventDraftUiState? = null,
        drawSettlementDraft: DrawSettlementDraftUiState? = null,
        settlementPrompt: SettlementPromptUiState? = null,
        horses: List<HorseUiModel> = emptyList(),
        horseBindingDraft: HorseBindingDraftUiState? = null,
        liveLogs: List<LiveLogItemUiModel> = emptyList()
    ): BattleScoreUiState {
        return BattleScoreUiState(
            currentRound = 1,
            windLabelRes = R.string.battle_wind_east,
            isPlayersLoaded = isPlayersLoaded,
            players = listOf(
                PlayerCardUiModel(
                    id = 1,
                    name = "A",
                    avatarKey = "player_1",
                    avatarEmoji = "P1",
                    roundDelta = "+0",
                    totalScore = "100",
                    isDealer = true,
                    status = PlayerStatus.ACTIVE,
                    winOrder = null
                ),
                PlayerCardUiModel(
                    id = 2,
                    name = "B",
                    avatarKey = "player_2",
                    avatarEmoji = "P2",
                    roundDelta = "+0",
                    totalScore = "100",
                    isDealer = false,
                    status = PlayerStatus.ACTIVE,
                    winOrder = null
                ),
                PlayerCardUiModel(
                    id = 3,
                    name = "C",
                    avatarKey = "player_3",
                    avatarEmoji = "P3",
                    roundDelta = "+0",
                    totalScore = "100",
                    isDealer = false,
                    status = PlayerStatus.HU,
                    winOrder = 1
                ),
                PlayerCardUiModel(
                    id = 4,
                    name = "D",
                    avatarKey = "player_4",
                    avatarEmoji = "P4",
                    roundDelta = "+0",
                    totalScore = "100",
                    isDealer = false,
                    status = PlayerStatus.ACTIVE,
                    winOrder = null
                )
            ),
            horses = horses,
            horseBindingDraft = horseBindingDraft,
            liveLogs = liveLogs,
            hapticsEnabled = false,
            lastAction = null,
            requiresPlayerSetup = requiresPlayerSetup,
            canUndo = false,
            canSettle = canSettle,
            playerSwapDialogVisible = false,
            multiplierRange = 1..8,
            eventDraft = eventDraft,
            drawSettlementDraft = drawSettlementDraft,
            settlementPrompt = settlementPrompt,
            resetAllConfirmStep = null
        )
    }
}
