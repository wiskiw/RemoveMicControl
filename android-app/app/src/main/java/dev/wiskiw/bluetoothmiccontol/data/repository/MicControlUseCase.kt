package dev.wiskiw.bluetoothmiccontol.data.repository

import android.media.AudioManager
import android.util.Log
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction
import dev.wiskiw.bluetoothmiccontol.service.ControlActionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MicControlUseCase(
    private val audioManager: AudioManager,
) : ControlActionHandler {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicCtrl"

        private val MUTE_CONTROL_ACTION = ControlAction.VOLUME_DOWN
        private val UNMUTE_CONTROL_ACTION = ControlAction.VOLUME_UP
    }

    private val micOffFlow = MutableStateFlow(audioManager.isMicrophoneMute)
    private val isVolumeMicControlEnabled = MutableStateFlow(true)

    fun getMicOffFlow(): Flow<Boolean> {
        return micOffFlow
    }

    fun getVolumeMicControlEnabledFlow(): Flow<Boolean> {
        return isVolumeMicControlEnabled
    }

    fun muteMic(mute: Boolean) {
        Log.d(LOG_TAG, "mic muted: $mute")
        micOffFlow.value = mute
        audioManager.isMicrophoneMute = mute
    }

    fun toggleMic() {
        muteMic(!micOffFlow.value)
    }

    fun toggleMicVolumeControl() {
        isVolumeMicControlEnabled.value = !isVolumeMicControlEnabled.value
    }

    override fun handleAction(action: ControlAction) {
        if (isVolumeMicControlEnabled.value) {
            when (action) {
                MUTE_CONTROL_ACTION -> muteMic(true)
                UNMUTE_CONTROL_ACTION -> muteMic(false)
                else -> {
                    // ignore
                }
            }
        }
    }
}
