package com.ash.mahjong.feature.battle_score.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ash.mahjong.R
import com.ash.mahjong.feature.battle_score.state.ResetAllConfirmStep
import com.ash.mahjong.feature.battle_score.ui.BattleScoreTestTags

@Composable
fun ResetAllConfirmDialog(
    step: ResetAllConfirmStep,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (titleRes, messageRes, confirmRes) = when (step) {
        ResetAllConfirmStep.FIRST -> Triple(
            R.string.battle_reset_confirm_step1_title,
            R.string.battle_reset_confirm_step1_message,
            R.string.battle_reset_confirm_step1_action
        )

        ResetAllConfirmStep.SECOND -> Triple(
            R.string.battle_reset_confirm_step2_title,
            R.string.battle_reset_confirm_step2_message,
            R.string.battle_reset_confirm_step2_action
        )
    }

    AlertDialog(
        modifier = Modifier.testTag(BattleScoreTestTags.RESET_CONFIRM_DIALOG),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(confirmRes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.battle_reset_confirm_cancel))
            }
        }
    )
}
