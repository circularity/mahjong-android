package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.navigation.TopLevelTabUiModel
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
fun BattleBottomBar(
    items: List<TopLevelTabUiModel>,
    onTabClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = MahjongDesign.elevation.bar,
        shadowElevation = MahjongDesign.elevation.bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            for ((index, item) in items.withIndex()) {
                BottomTab(
                    label = stringResource(item.labelRes),
                    iconRes = item.iconRes,
                    selected = item.selected,
                    iconTag = BattleScoreTestTags.bottomTabIcon(index),
                    modifier = Modifier.weight(1f),
                    onClick = { onTabClick(index) }
                )
            }
        }
    }
}

@Composable
private fun BottomTab(
    label: String,
    iconRes: Int,
    selected: Boolean,
    iconTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val spacing = MahjongDesign.spacing
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    TextButton(
        modifier = modifier,
        onClick = onClick,
        shape = MahjongDesign.shapes.secondaryAction,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                             else Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .testTag(iconTag)
                    .padding(bottom = spacing.xxs)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}
