package com.ash.mahjong.feature.player_list.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.ash.mahjong.R
import com.ash.mahjong.feature.player_list.state.PlayerListUiState
import com.ash.mahjong.ui.avatar.PlayerAvatarImageStorage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlayerListScreen(
    uiState: PlayerListUiState,
    onAddPlayerClick: () -> Unit,
    onDismissAddPlayerDialog: () -> Unit,
    onPlayerNameChange: (String) -> Unit,
    onDecreaseInitialScore: () -> Unit,
    onIncreaseInitialScore: () -> Unit,
    onSelectDialogAvatar: (String) -> Unit,
    onConfirmAddPlayer: () -> Unit,
    onTogglePlayerActiveClick: (Int) -> Unit,
    onTogglePlayerRoleClick: (Int) -> Unit,
    onChangePlayerAvatarClick: (Int) -> Unit,
    onPlayerLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showAvatarSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val saveAvatarToLocalStorage: (Uri, (() -> Unit)?) -> Unit = { sourceUri, onComplete ->
        coroutineScope.launch {
            val avatarKey = withContext(Dispatchers.IO) {
                PlayerAvatarImageStorage.saveCompressedAvatar(
                    context = context,
                    sourceUri = sourceUri
                )
            }
            if (avatarKey == null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.players_avatar_pick_failed),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                onSelectDialogAvatar(avatarKey)
            }
            onComplete?.invoke()
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            saveAvatarToLocalStorage(uri, null)
        }
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedUri = pendingCameraUri
        val capturedFile = pendingCameraFile
        pendingCameraUri = null
        pendingCameraFile = null
        if (success && capturedUri != null) {
            saveAvatarToLocalStorage(capturedUri) {
                capturedFile?.delete()
            }
        } else {
            capturedFile?.delete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlayerListColors.Background)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { AddPlayerCard(onClick = onAddPlayerClick) }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.players_active_count, uiState.activePlayerCount),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(R.string.players_icon_filter),
                        color = PlayerListColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            if (uiState.players.isEmpty()) {
                item { EmptyPlayersState() }
            } else {
                if (uiState.activePlayers.isNotEmpty()) {
                    item {
                        PlayerSectionTitle(text = stringResource(R.string.players_active_section))
                    }
                    uiState.activePlayers.forEach { player ->
                        item(key = player.id) {
                            PlayerListCard(
                                player = player,
                                onToggleActiveClick = { onTogglePlayerActiveClick(player.id) },
                                onToggleRoleClick = { onTogglePlayerRoleClick(player.id) },
                                onAvatarClick = { onChangePlayerAvatarClick(player.id) },
                                onLongClick = { onPlayerLongClick(player.id) }
                            )
                        }
                    }
                }
                if (uiState.inactivePlayers.isNotEmpty()) {
                    item {
                        PlayerSectionTitle(
                            text = stringResource(
                                R.string.players_inactive_section,
                                uiState.inactivePlayerCount
                            )
                        )
                    }
                    uiState.inactivePlayers.forEach { player ->
                        item(key = player.id) {
                            PlayerListCard(
                                player = player,
                                onToggleActiveClick = { onTogglePlayerActiveClick(player.id) },
                                onToggleRoleClick = { onTogglePlayerRoleClick(player.id) },
                                onAvatarClick = { onChangePlayerAvatarClick(player.id) },
                                onLongClick = { onPlayerLongClick(player.id) }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.players_total_registered),
                        value = uiState.totalPlayerCount.toString(),
                        emphasized = false
                    )

                    val monthlyBestValue = uiState.monthlyBestPlayerName.ifBlank {
                        stringResource(R.string.players_stats_none)
                    }
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.players_month_best),
                        value = monthlyBestValue,
                        emphasized = true
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(6.dp)) }
        }

        if (uiState.addPlayerDialog.visible) {
            AddPlayerDialog(
                dialogState = uiState.addPlayerDialog,
                onDismissRequest = onDismissAddPlayerDialog,
                onPlayerNameChange = onPlayerNameChange,
                onDecreaseInitialScore = onDecreaseInitialScore,
                onIncreaseInitialScore = onIncreaseInitialScore,
                onSelectAvatar = onSelectDialogAvatar,
                onSelectPhotoAvatarClick = {
                    showAvatarSourceDialog = true
                },
                onConfirmAddPlayer = onConfirmAddPlayer
            )
        }

        if (showAvatarSourceDialog) {
            AvatarSourceDialog(
                onDismissRequest = { showAvatarSourceDialog = false },
                onPickFromGallery = {
                    showAvatarSourceDialog = false
                    pickPhotoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTakePhoto = {
                    showAvatarSourceDialog = false
                    val output = createCameraOutput(context)
                    if (output == null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.players_avatar_pick_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        pendingCameraUri = output.uri
                        pendingCameraFile = output.file
                        takePhotoLauncher.launch(output.uri)
                    }
                }
            )
        }
    }
}

@Composable
private fun PlayerSectionTitle(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        text = text,
        color = PlayerListColors.OnSurfaceVariant,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    )
}

@Composable
private fun EmptyPlayersState() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        text = stringResource(R.string.players_empty_state),
        color = PlayerListColors.OnSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
}

@Composable
private fun AvatarSourceDialog(
    onDismissRequest: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.players_avatar_source_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.players_avatar_content_description))
        },
        confirmButton = {
            TextButton(onClick = onPickFromGallery) {
                Text(text = stringResource(R.string.players_avatar_source_gallery))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onTakePhoto) {
                    Text(text = stringResource(R.string.players_avatar_source_camera))
                }
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(R.string.players_avatar_source_cancel))
                }
            }
        }
    )
}

private data class CameraOutput(
    val file: File,
    val uri: Uri
)

private fun createCameraOutput(context: Context): CameraOutput? {
    val tempDir = File(context.cacheDir, "player_avatar_capture").apply { mkdirs() }
    val tempFile = File(tempDir, "capture_${System.currentTimeMillis()}.jpg")
    val fileProviderAuthority = "${context.packageName}.fileprovider"
    val contentUri = runCatching {
        FileProvider.getUriForFile(context, fileProviderAuthority, tempFile)
    }.getOrNull() ?: return null
    return CameraOutput(
        file = tempFile,
        uri = contentUri
    )
}
