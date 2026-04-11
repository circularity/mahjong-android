package com.ash.mahjong.feature.player_list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ash.mahjong.R
import com.ash.mahjong.data.player.PlayerRole
import com.ash.mahjong.feature.player_list.state.PlayerListItemUiModel
import com.ash.mahjong.feature.player_list.state.PlayerTrend
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual
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
@OptIn(ExperimentalFoundationApi::class)
internal fun PlayerListCard(
    player: PlayerListItemUiModel,
    onToggleActiveClick: () -> Unit,
    onToggleRoleClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val containerColor = if (player.isActive) Color.White else Color(0xFFF4F5F7)
    val scoreColor = if (player.isActive) PlayerListColors.Primary else PlayerListColors.OnSurfaceVariant
    val cardAlpha = if (player.isActive) 1f else 0.78f
    val cardShape = RoundedCornerShape(24.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Avatar(
                    avatarKey = player.avatarKey,
                    avatarEmoji = player.avatarEmoji,
                    fallbackText = player.name.take(1),
                    isOnline = player.isOnline,
                    trend = player.trend,
                    onClick = onAvatarClick
                )
                Text(
                    text = player.name,
                    color = PlayerListColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                modifier = Modifier.align(Alignment.Bottom),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                PlayerToggleButton(
                    text = if (player.isActive) {
                        stringResource(R.string.players_status_active)
                    } else {
                        stringResource(R.string.players_status_inactive)
                    },
                    icon = if (player.isActive) {
                        Icons.Outlined.PauseCircle
                    } else {
                        Icons.Outlined.PlayCircle
                    },
                    containerColor = if (player.isActive) {
                        Color(0xFFDBF0D5)
                    } else {
                        Color(0xFFE6E8ED)
                    },
                    contentColor = if (player.isActive) {
                        Color(0xFF2E5D2A)
                    } else {
                        Color(0xFF4F5661)
                    },
                    onClick = onToggleActiveClick
                )
                PlayerToggleButton(
                    text = if (player.playerRole == PlayerRole.ON_TABLE) {
                        stringResource(R.string.players_role_on_table)
                    } else {
                        stringResource(R.string.players_role_horse)
                    },
                    trailingText = if (player.playerRole == PlayerRole.ON_TABLE) {
                        stringResource(R.string.players_role_horse)
                    } else {
                        stringResource(R.string.players_role_on_table)
                    },
                    icon = Icons.Outlined.SwapHoriz,
                    containerColor = if (player.playerRole == PlayerRole.ON_TABLE) {
                        Color(0xFFDCE8FF)
                    } else {
                        Color(0xFFFFE8CC)
                    },
                    contentColor = if (player.playerRole == PlayerRole.ON_TABLE) {
                        Color(0xFF31518A)
                    } else {
                        Color(0xFF7A4B1C)
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun Avatar(
    avatarKey: String,
    avatarEmoji: String,
    fallbackText: String,
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
            PlayerAvatarVisual(
                avatarKey = avatarKey,
                avatarEmoji = avatarEmoji,
                fallbackText = fallbackText,
                textColor = PlayerListColors.OnSurface,
                textStyle = TextStyle(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
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
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun PlayerToggleButton(
    text: String,
    icon: ImageVector,
    trailingText: String? = null,
    containerColor: Color = Color(0xFFF0F3F8),
    contentColor: Color = PlayerListColors.OnSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            trailingText?.let { value ->
                Text(
                    text = value,
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
