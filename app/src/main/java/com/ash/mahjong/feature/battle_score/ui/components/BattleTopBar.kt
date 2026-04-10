package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
fun BattleTopBar(
    currentRound: Int,
    canSettle: Boolean,
    onSettleClick: () -> Unit,
    onQuickPlayersClick: () -> Unit,
    onQuickHistoryClick: () -> Unit,
    onQuickSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(MahjongDesign.spacing.topBarHeight)
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = MahjongDesign.elevation.bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                IconButton(onClick = onQuickPlayersClick) {
                    Icon(
                        painter = painterResource(R.drawable.players),
                        contentDescription = stringResource(R.string.battle_nav_rules),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onQuickHistoryClick) {
                    Icon(
                        painter = painterResource(R.drawable.match),
                        contentDescription = stringResource(R.string.battle_history_entry),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = stringResource(R.string.battle_round_label, currentRound),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                FilledTonalButton(
                    onClick = onSettleClick,
                    enabled = canSettle,
                    modifier = Modifier.testTag(BattleScoreTestTags.FLOATING_ACTION)
                ) {
                    Text(text = stringResource(R.string.battle_fab_settle))
                }
                IconButton(onClick = onQuickSettingsClick) {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = stringResource(R.string.battle_nav_settings),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
