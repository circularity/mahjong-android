package com.ash.mahjong.feature.player_list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ash.mahjong.R
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.feature.player_list.state.AddPlayerDialogUiState
import com.ash.mahjong.feature.player_list.state.PlayerDialogMode
import com.ash.mahjong.ui.avatar.PlayerAvatarVisual
import java.text.NumberFormat

@Composable
internal fun AddPlayerDialog(
    dialogState: AddPlayerDialogUiState,
    onDismissRequest: () -> Unit,
    onPlayerNameChange: (String) -> Unit,
    onDecreaseInitialScore: () -> Unit,
    onIncreaseInitialScore: () -> Unit,
    onSelectAvatar: (String) -> Unit,
    onSelectPhotoAvatarClick: () -> Unit,
    onConfirmAddPlayer: () -> Unit
) {
    val dialogTitleRes = if (dialogState.mode == PlayerDialogMode.EDIT) {
        R.string.players_dialog_edit_title
    } else {
        R.string.players_dialog_title
    }
    val dialogConfirmRes = if (dialogState.mode == PlayerDialogMode.EDIT) {
        R.string.players_dialog_confirm_edit
    } else {
        R.string.players_dialog_confirm
    }
    val avatarContentDescription = stringResource(R.string.players_avatar_content_description)
    val selectedAvatarFallback = dialogState.playerName.trim().take(1).ifBlank {
        stringResource(R.string.settings_profile_placeholder)
    }
    val selectedAvatarIsImage = PlayerAnimalAvatarCatalog.isVisualImageAvatarKey(
        dialogState.selectedAvatarKey
    )
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EEF5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(dialogTitleRes),
                    color = PlayerListColors.Primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(18.dp))

                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (selectedAvatarIsImage) Color.Black else Color(0xFF15324A)),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayerAvatarVisual(
                            avatarKey = dialogState.selectedAvatarKey,
                            avatarEmoji = dialogState.selectedAvatarEmoji,
                            fallbackText = selectedAvatarFallback,
                            contentDescription = avatarContentDescription,
                            textStyle = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                            textColor = Color.White,
                            modifier = if (selectedAvatarIsImage) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.size(72.dp)
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFBCEFAE))
                            .clickable(onClick = onSelectPhotoAvatarClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = stringResource(
                                R.string.players_dialog_avatar_pick_photo
                            ),
                            tint = PlayerListColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.players_dialog_avatar_label),
                            color = PlayerListColors.OnSurfaceVariant.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        AvatarLibraryGrid(
                            selectedAvatarKey = dialogState.selectedAvatarKey,
                            onSelectAvatar = onSelectAvatar,
                            avatarContentDescription = avatarContentDescription,
                            fallbackText = selectedAvatarFallback
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.players_dialog_name_label),
                            color = PlayerListColors.OnSurfaceVariant.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextField(
                            value = dialogState.playerName,
                            onValueChange = onPlayerNameChange,
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.players_dialog_name_placeholder),
                                    color = PlayerListColors.OnSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFDCE8F5),
                                unfocusedContainerColor = Color(0xFFDCE8F5),
                                disabledContainerColor = Color(0xFFDCE8F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = PlayerListColors.OnSurface,
                                unfocusedTextColor = PlayerListColors.OnSurface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        dialogState.errorMessageRes?.let { errorRes ->
                            Text(
                                text = stringResource(errorRes),
                                color = Color(0xFF9F403D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.players_dialog_score_label),
                            color = PlayerListColors.OnSurfaceVariant.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ScoreControlButton(
                                    symbol = stringResource(R.string.players_icon_trend_down),
                                    onClick = onDecreaseInitialScore
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatScore(dialogState.initialScore),
                                        color = PlayerListColors.Primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 34.sp
                                    )
                                    Text(
                                        text = stringResource(R.string.players_points_unit),
                                        color = PlayerListColors.OnSurfaceVariant.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                                ScoreControlButton(
                                    symbol = stringResource(R.string.players_icon_trend_up),
                                    onClick = onIncreaseInitialScore,
                                    emphasized = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onConfirmAddPlayer,
                    enabled = dialogState.canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayerListColors.Primary,
                        disabledContainerColor = PlayerListColors.Primary.copy(alpha = 0.45f),
                        contentColor = Color.White,
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(dialogConfirmRes),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(R.string.players_dialog_cancel),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarLibraryGrid(
    selectedAvatarKey: String,
    onSelectAvatar: (String) -> Unit,
    avatarContentDescription: String,
    fallbackText: String
) {
    val avatarKeys = PlayerAnimalAvatarCatalog.allAvatarKeys()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        avatarKeys.chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { avatarKey ->
                    AvatarChoiceItem(
                        avatarKey = avatarKey,
                        selected = selectedAvatarKey == avatarKey,
                        avatarContentDescription = avatarContentDescription,
                        fallbackText = fallbackText,
                        onClick = { onSelectAvatar(avatarKey) }
                    )
                }
                repeat(5 - row.size) {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }
    }
}

@Composable
private fun AvatarChoiceItem(
    avatarKey: String,
    selected: Boolean,
    avatarContentDescription: String,
    fallbackText: String,
    onClick: () -> Unit
) {
    val isImageAvatar = PlayerAnimalAvatarCatalog.isVisualImageAvatarKey(avatarKey)
    val backgroundColor = if (isImageAvatar) Color.Black else Color(0xFF15324A)
    val borderColor = if (selected) {
        Color(0xFFBCEFAE)
    } else {
        Color(0xFF3F6F9C).copy(alpha = 0.45f)
    }
    val avatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(avatarKey)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isImageAvatar) {
            PlayerAvatarVisual(
                avatarKey = avatarKey,
                avatarEmoji = avatarEmoji,
                fallbackText = fallbackText,
                contentDescription = avatarContentDescription,
                textStyle = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                textColor = Color.White,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = avatarEmoji.ifBlank { fallbackText },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScoreControlButton(
    symbol: String,
    onClick: () -> Unit,
    emphasized: Boolean = false
) {
    val container = if (emphasized) Color(0xFFBCEFAE) else Color(0xFFDCE8F5)
    val textColor = if (emphasized) PlayerListColors.Primary else PlayerListColors.OnSurfaceVariant

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = container
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }
    }
}

private fun formatScore(score: Int): String {
    return NumberFormat.getIntegerInstance().format(score)
}
