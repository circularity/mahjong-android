package com.ash.mahjong.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = BattlePrimary,
    onPrimary = BattleOnPrimary,
    primaryContainer = BattlePrimaryContainer,
    onPrimaryContainer = BattleOnPrimaryContainer,
    secondary = BattleSecondary,
    onSecondary = BattleOnSecondary,
    secondaryContainer = BattleSecondaryContainer,
    onSecondaryContainer = BattleOnSecondaryContainer,
    tertiary = BattleTertiary,
    onTertiary = BattleOnTertiary,
    tertiaryContainer = BattleTertiaryContainer,
    onTertiaryContainer = BattleOnTertiaryContainer,
    error = BattleError,
    onError = BattleOnError,
    errorContainer = BattleErrorContainer,
    onErrorContainer = BattleOnErrorContainer,
    background = BattleBackground,
    onBackground = BattleOnBackground,
    surface = BattleSurface,
    onSurface = BattleOnSurface,
    surfaceVariant = BattleSurfaceVariant,
    onSurfaceVariant = BattleOnSurfaceVariant,
    outline = BattleOutline,
    surfaceContainerLow = BattleSurfaceContainerLow,
    surfaceContainer = BattleSurfaceContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = BattlePrimary,
    onPrimary = BattleOnPrimary,
    primaryContainer = BattlePrimaryContainer,
    onPrimaryContainer = BattleOnPrimaryContainer,
    secondary = BattleSecondary,
    onSecondary = BattleOnSecondary,
    secondaryContainer = BattleSecondaryContainer,
    onSecondaryContainer = BattleOnSecondaryContainer,
    tertiary = BattleTertiary,
    onTertiary = BattleOnTertiary,
    tertiaryContainer = BattleTertiaryContainer,
    onTertiaryContainer = BattleOnTertiaryContainer,
    error = BattleError,
    onError = BattleOnError,
    errorContainer = BattleErrorContainer,
    onErrorContainer = BattleOnErrorContainer,
    background = BattleBackground,
    onBackground = BattleOnBackground,
    surface = BattleSurface,
    onSurface = BattleOnSurface,
    surfaceVariant = BattleSurfaceVariant,
    onSurfaceVariant = BattleOnSurfaceVariant,
    outline = BattleOutline,
    surfaceContainerLow = BattleSurfaceContainerLow,
    surfaceContainer = BattleSurfaceContainer
)

@Composable
fun MahjongTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalMahjongSpacing provides MahjongSpacing(),
        LocalMahjongShapes provides MahjongShapes(),
        LocalMahjongElevation provides MahjongElevation()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
