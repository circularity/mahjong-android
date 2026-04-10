package com.ash.mahjong.feature.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ash.mahjong.BuildConfig
import com.ash.mahjong.data.settings.GameSettingsRepository
import com.ash.mahjong.feature.settings.intent.SettingsIntent
import com.ash.mahjong.feature.settings.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val gameSettingsRepository: GameSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(versionName = BuildConfig.VERSION_NAME))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gameSettingsRepository.observeSettings().collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        basePoint = settings.basePoint,
                        cappingMultiplier = settings.cappingMultiplier,
                        hapticsEnabled = settings.hapticsEnabled
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OnDecreaseBasePoint -> updateBasePoint(uiState.value.basePoint - 1)
            SettingsIntent.OnIncreaseBasePoint -> updateBasePoint(uiState.value.basePoint + 1)
            SettingsIntent.OnDecreaseCappingMultiplier -> {
                updateCappingMultiplier(uiState.value.cappingMultiplier - 1)
            }

            SettingsIntent.OnIncreaseCappingMultiplier -> {
                updateCappingMultiplier(uiState.value.cappingMultiplier + 1)
            }

            is SettingsIntent.OnHapticsEnabledChange -> {
                viewModelScope.launch {
                    gameSettingsRepository.updateHapticsEnabled(intent.enabled)
                }
            }

            SettingsIntent.OnClearCacheClick -> {
                viewModelScope.launch {
                    gameSettingsRepository.reset()
                }
            }
        }
    }

    private fun updateBasePoint(basePoint: Int) {
        viewModelScope.launch {
            gameSettingsRepository.updateBasePoint(basePoint)
        }
    }

    private fun updateCappingMultiplier(multiplier: Int) {
        viewModelScope.launch {
            gameSettingsRepository.updateCappingMultiplier(multiplier)
        }
    }
}
