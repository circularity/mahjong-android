package com.ash.mahjong.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ash.mahjong.R
import com.ash.mahjong.feature.settings.intent.SettingsIntent
import com.ash.mahjong.feature.settings.state.SettingsUiState

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    fun runWithHaptic(enabled: Boolean = uiState.hapticsEnabled, action: () -> Unit) {
        action()
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = modifier
            .background(SettingsColors.Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SettingsColors.Primary,
                modifier = Modifier.testTag(SettingsTestTags.SCREEN_TITLE)
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_game_rules)) {
            StepperRow(
                title = stringResource(R.string.settings_bottom_point_label),
                value = uiState.basePoint,
                canDecrease = uiState.canDecreaseBasePoint,
                canIncrease = uiState.canIncreaseBasePoint,
                valueTag = SettingsTestTags.BASE_POINT_VALUE,
                increaseTag = SettingsTestTags.BASE_POINT_INCREASE,
                onDecrease = {
                    runWithHaptic {
                        onIntent(SettingsIntent.OnDecreaseBasePoint)
                    }
                },
                onIncrease = {
                    runWithHaptic {
                        onIntent(SettingsIntent.OnIncreaseBasePoint)
                    }
                }
            )

            StepperRow(
                title = stringResource(R.string.settings_capping_label),
                value = uiState.cappingFan,
                canDecrease = uiState.canDecreaseCappingFan,
                canIncrease = uiState.canIncreaseCappingFan,
                valueTag = SettingsTestTags.CAPPING_VALUE,
                increaseTag = SettingsTestTags.CAPPING_INCREASE,
                onDecrease = {
                    runWithHaptic {
                        onIntent(SettingsIntent.OnDecreaseCappingFan)
                    }
                },
                onIncrease = {
                    runWithHaptic {
                        onIntent(SettingsIntent.OnIncreaseCappingFan)
                    }
                }
            )
        }

        SettingsSection(title = stringResource(R.string.settings_section_feedback)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_haptic_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = SettingsColors.OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = uiState.hapticsEnabled,
                    onCheckedChange = { enabled ->
                        onIntent(SettingsIntent.OnHapticsEnabledChange(enabled))
                        if (enabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    modifier = Modifier.testTag(SettingsTestTags.HAPTICS_SWITCH)
                )
            }
        }

        SettingsSection(title = stringResource(R.string.settings_section_about)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_version_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = SettingsColors.OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_version_value, uiState.versionName),
                    style = MaterialTheme.typography.labelLarge,
                    color = SettingsColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = SettingsColors.Divider)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        runWithHaptic {
                            onIntent(SettingsIntent.OnClearCacheClick)
                        }
                    }
                    .padding(vertical = 12.dp)
                    .testTag(SettingsTestTags.CLEAR_CACHE),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_clear_cache_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_clear_cache_suffix),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = SettingsColors.Divider)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_about_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = SettingsColors.OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_about_suffix),
                    style = MaterialTheme.typography.labelLarge,
                    color = SettingsColors.OnSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.settings_footer_mahjong_tiles),
                contentDescription = stringResource(R.string.settings_footer_image_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x33000000))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_footer_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_footer_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = SettingsColors.OnSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    valueTag: String,
    increaseTag: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = SettingsColors.OnSurface,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(
                label = stringResource(R.string.settings_stepper_decrease),
                enabled = canDecrease,
                onClick = onDecrease
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = SettingsColors.OnSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag(valueTag)
            )
            StepperButton(
                label = stringResource(R.string.settings_stepper_increase),
                enabled = canIncrease,
                onClick = onIncrease,
                modifier = Modifier.testTag(increaseTag)
            )
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(width = 36.dp, height = 32.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SettingsColors.SurfaceContainer,
            contentColor = SettingsColors.Primary
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private object SettingsColors {
    val Background = Color(0xFFF6F9FF)
    val Primary = Color(0xFF3B6934)
    val OnSurface = Color(0xFF233442)
    val OnSurfaceVariant = Color(0xFF506170)
    val SurfaceContainer = Color(0xFFE4EFFC)
    val Divider = Color(0x1F506170)
}
