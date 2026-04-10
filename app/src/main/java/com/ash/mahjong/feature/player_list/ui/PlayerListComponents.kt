package com.ash.mahjong.feature.player_list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ash.mahjong.R
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.feature.player_list.state.PlayerListItemUiModel
import com.ash.mahjong.feature.player_list.state.PlayerTrend
import java.text.NumberFormat

@Composable
internal fun AddPlayerCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFBCEFAE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.players_icon_add_user),
                        color = PlayerListColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.players_add_title),
                        color = PlayerListColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.players_add_subtitle),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                text = stringResource(R.string.players_icon_chevron),
                color = PlayerListColors.Primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun PlayerListCard(
    player: PlayerListItemUiModel,
    onToggleActiveClick: () -> Unit,
    onToggleRoleClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val containerColor = if (player.isActive) Color.White else Color(0xFFF4F5F7)
    val scoreColor = if (player.isActive) PlayerListColors.Primary else PlayerListColors.OnSurfaceVariant
    val cardAlpha = if (player.isActive) 1f else 0.78f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar(
                avatarEmoji = player.avatarEmoji,
                isOnline = player.isOnline,
                trend = player.trend,
                onClick = onAvatarClick
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.name,
                        color = PlayerListColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (player.isMvp) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFBFECD2)
                        ) {
                            Text(
                                text = stringResource(R.string.players_mvp_tag),
                                color = Color(0xFF315A45),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    PlayerStatusTag(isActive = player.isActive)
                    PlayerRoleTag(role = player.playerRole)
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatScore(player.score),
                        color = scoreColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 27.sp
                    )
                    Text(
                        text = stringResource(R.string.players_points_unit),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trendText(player.trend) + stringResource(
                            R.string.players_win_rate,
                            player.winRatePercent
                        ),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.players_total_games, player.totalGames),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                PlayerToggleButton(
                    text = if (player.isActive) {
                        stringResource(R.string.players_status_active)
                    } else {
                        stringResource(R.string.players_status_inactive)
                    },
                    onClick = onToggleActiveClick
                )
                PlayerToggleButton(
                    text = if (player.playerRole == PlayerRole.ON_TABLE) {
                        stringResource(R.string.players_role_on_table)
                    } else {
                        stringResource(R.string.players_role_horse)
                    },
                    onClick = onToggleRoleClick
                )
            }
        }
    }
}

@Composable
private fun PlayerStatusTag(isActive: Boolean) {
    val containerColor = if (isActive) Color(0xFFDBF0D5) else Color(0xFFE6E8ED)
    val contentColor = if (isActive) Color(0xFF2E5D2A) else Color(0xFF4F5661)
    val text = if (isActive) {
        stringResource(R.string.players_status_active)
    } else {
        stringResource(R.string.players_status_inactive)
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun Avatar(
    avatarEmoji: String,
    isOnline: Boolean,
    trend: PlayerTrend,
    onClick: () -> Unit
) {
    val avatarBackground = when (trend) {
        PlayerTrend.UP -> Color(0xFFF4FAF1)
        PlayerTrend.FLAT -> Color(0xFFFFF7EE)
        PlayerTrend.DOWN -> Color(0xFFFAF5EE)
    }
    val avatarContentDescription = stringResource(R.string.players_avatar_content_description)
    val avatarBorderColor = Color(0x1F233442)

    Box {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(avatarBackground)
                .border(
                    width = 1.dp,
                    color = avatarBorderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription = avatarContentDescription
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarEmoji,
                color = PlayerListColors.OnSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(PlayerListColors.Primary)
            )
        }
    }
}

@Composable
private fun PlayerRoleTag(role: PlayerRole) {
    val (containerColor, contentColor, text) = if (role == PlayerRole.ON_TABLE) {
        Triple(
            Color(0xFFDCE8FF),
            Color(0xFF31518A),
            stringResource(R.string.players_role_on_table)
        )
    } else {
        Triple(
            Color(0xFFFFE8CC),
            Color(0xFF7A4B1C),
            stringResource(R.string.players_role_horse)
        )
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PlayerToggleButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF0F3F8))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = PlayerListColors.OnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun StatsCard(
    modifier: Modifier,
    title: String,
    value: String,
    emphasized: Boolean
) {
    val containerColor = if (emphasized) Color(0xFFAEE2A0) else Color.White
    val textColor = if (emphasized) Color(0xFF2F5C29) else PlayerListColors.OnSurface

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
            Text(
                text = title,
                color = textColor.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun trendText(trend: PlayerTrend): String {
    return when (trend) {
        PlayerTrend.UP -> stringResource(R.string.players_icon_trend_up)
        PlayerTrend.FLAT -> stringResource(R.string.players_icon_trend_flat)
        PlayerTrend.DOWN -> stringResource(R.string.players_icon_trend_down)
    }
}

internal object PlayerListColors {
    val Background = Color(0xFFF6F9FF)
    val Primary = Color(0xFF3B6934)
    val OnSurface = Color(0xFF233442)
    val OnSurfaceVariant = Color(0xFF506170)
}

private fun formatScore(score: Int): String {
    return NumberFormat.getIntegerInstance().format(score)
}
