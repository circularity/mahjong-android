package com.ash.mahjong.feature.stats.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ash.mahjong.data.battle.BattleRecordRepository
import com.ash.mahjong.data.battle.PlayerStats
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import com.ash.mahjong.feature.stats.state.PlayerStatsUiModel
import com.ash.mahjong.feature.stats.state.StatsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val battleRecordRepository: BattleRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            battleRecordRepository.observePlayerStats().collect { stats ->
                _uiState.update { state ->
                    state.copy(players = stats.map(::toUiModel))
                }
            }
        }
    }

    private fun toUiModel(stats: PlayerStats): PlayerStatsUiModel {
        val resolvedAvatarKey = PlayerAnimalAvatarCatalog.resolveAvatarKeyOrFallback(
            avatarKey = stats.avatarKey,
            playerId = stats.playerId,
            createdAt = stats.createdAt
        )
        return PlayerStatsUiModel(
            playerId = stats.playerId,
            name = stats.name,
            avatarKey = resolvedAvatarKey,
            avatarEmoji = PlayerAnimalAvatarCatalog.emojiForKey(resolvedAvatarKey),
            zimoRounds = stats.zimoRounds,
            huRounds = stats.huRounds,
            gangRounds = stats.gangRounds,
            totalRounds = stats.totalRounds,
            winRounds = stats.winRounds,
            dianPaoRounds = stats.dianPaoRounds,
            winRateProgress = (stats.winRate / 100f).coerceIn(0f, 1f),
            winRateText = formatPercent(stats.winRate),
            totalDelta = stats.totalDelta,
            totalDeltaText = formatSigned(stats.totalDelta),
            avgDeltaText = formatSigned(stats.avgDelta),
            recentRoundsText = stats.recentRounds
                .take(5)
                .joinToString(separator = "  ") { value -> formatSigned(value) },
            lastBattleTimeText = stats.lastBattleAt
                ?.let(::formatDateTime)
                .orEmpty(),
            recentRounds = stats.recentRounds.take(10)
        )
    }

    private fun formatSigned(value: Int): String {
        val formatted = NumberFormat.getIntegerInstance().format(kotlin.math.abs(value))
        return if (value >= 0) "+$formatted" else "-$formatted"
    }

    private fun formatSigned(value: Float): String {
        val absValue = kotlin.math.abs(value)
        val rounded = String.format("%.1f", absValue)
        return if (value >= 0f) "+$rounded" else "-$rounded"
    }

    private fun formatPercent(value: Float): String {
        return "${value.roundToInt()}%"
    }

    private fun formatDateTime(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(lastBattleTimeFormatter)
    }

    companion object {
        private val lastBattleTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
    }
}
