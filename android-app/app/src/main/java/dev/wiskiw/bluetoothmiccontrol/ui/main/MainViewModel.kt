package dev.wiskiw.bluetoothmiccontrol.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.wiskiw.bluetoothmiccontrol.data.repository.MicControlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(
    private val micControlRepository: MicControlRepository,
) : ViewModel() {

    private val _isEnableMicCheckedFlow = MutableStateFlow(false)
    val isEnableMicCheckedFlow = _isEnableMicCheckedFlow.asStateFlow()

    private val _isVolumeMicControlEnabledFlow = MutableStateFlow(false)
    val isVolumeMicControlEnabledFlow = _isVolumeMicControlEnabledFlow.asStateFlow()

    private val _isControlsActiveFlow = MutableStateFlow(false)
    val isControlsActiveFlow = _isControlsActiveFlow.asStateFlow()

    init {
        micControlRepository.getIsMicMutedFlowFlow()
            .onEach { isMuted -> _isEnableMicCheckedFlow.value = !isMuted }
            .launchIn(viewModelScope)

        micControlRepository.getIsVolumeMicControlEnabledFlow()
            .onEach { _isVolumeMicControlEnabledFlow.value = it }
            .launchIn(viewModelScope)

        micControlRepository.getIsUserInCommunicationFlow()
            .onEach { _isControlsActiveFlow.value = it }
            .launchIn(viewModelScope)
    }

    fun onEnableMicSwitched(isChecked: Boolean) {
        micControlRepository.setMicMuted(mute = !isChecked)
    }

    fun onEnableVolumeControlSwitched(isChecked: Boolean) {
        micControlRepository.setMicVolumeControlEnabled(enabled = isChecked)
    }
}
