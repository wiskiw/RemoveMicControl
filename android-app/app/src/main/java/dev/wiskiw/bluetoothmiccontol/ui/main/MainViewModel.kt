package dev.wiskiw.bluetoothmiccontol.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(
    private val micControlUseCase: MicControlUseCase,
) : ViewModel() {

    private val _isMicOffFlow = MutableStateFlow(false)
    val isMicOffFlow = _isMicOffFlow.asStateFlow()

    init {
        micControlUseCase.getMicOffFlow()
            .onEach { _isMicOffFlow.value = it }
            .launchIn(viewModelScope)
    }

    fun onMuteMicSwitched(isChecked: Boolean) {
        micControlUseCase.muteMic(mute = isChecked)
    }
}
