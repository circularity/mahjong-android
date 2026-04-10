package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.state.HorseUiModel
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual
import com.ash.mahjong.ui.theme.MahjongDesign

@Composable
fun HorseSection(
    horses: List<HorseUiModel>,
    onBindClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MahjongDesign.spacing
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Text(
            text = stringResource(R.string.battle_horse_section_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            items(
                items = horses,
                key = { horse -> horse.id }
            ) { horse ->
                HorseCard(
                    horse = horse,
                    onBindClick = { onBindClick(horse.id) }
                )
            }
        }
    }
}

@Composable
private fun HorseCard(
    horse: HorseUiModel,
    onBindClick: () -> Unit
) {
    val spacing = MahjongDesign.spacing
    val avatarContentDescription = stringResource(R.string.player_avatar_content_desc)
    val labelText = horse.boundOnTablePlayerName?.let { boundPlayerName ->
        stringResource(R.string.battle_horse_item_bound_template, horse.name, boundPlayerName)
    } ?: stringResource(R.string.battle_horse_item_idle_template, horse.name)

    Card(
        modifier = Modifier
            .testTag(BattleScoreTestTags.horseCard(horse.id))
            .clickable(onClick = onBindClick),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .semantics { contentDescription = avatarContentDescription },
                    contentAlignment = Alignment.Center
                ) {
                    PlayerAvatarVisual(
                        avatarKey = horse.avatarKey,
                        avatarEmoji = horse.avatarEmoji,
                        fallbackText = horse.name.take(1),
                        textStyle = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.battle_horse_round_delta_template, horse.roundDelta),
                        style = MaterialTheme.typography.labelSmall,
                        color = scoreColor(horse.roundDelta)
                    )
                    Text(
                        text = stringResource(R.string.battle_settlement_total_score, horse.totalScore),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
