package dev.wiskiw.bluetoothmiccontol.data.repository

import android.content.Context
import android.media.AudioManager
import android.util.Log
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction
import dev.wiskiw.bluetoothmiccontol.service.MicControlsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MicControlRepository(
    private val appContext: Context,
    private val audioManager: AudioManager,
) {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicRep"

        private val MUTE_CONTROL_ACTION = ControlAction.VOLUME_DOWN
        private val UNMUTE_CONTROL_ACTION = ControlAction.VOLUME_UP
    }

    private val repositoryJob = Job()
    private val repositoryScope = CoroutineScope(Dispatchers.Main + repositoryJob)

    private val micOffFlow = MutableStateFlow(audioManager.isMicrophoneMute)
    private val isMicVolumeControlEnabled = MutableStateFlow(true)

    init {
        isMicVolumeControlEnabled
            .onEach { isEnabled ->
                if (isEnabled) {
                    startControlsService()
                }
            }
            .launchIn(repositoryScope)
    }

    fun getMicOffFlow(): Flow<Boolean> {
        return micOffFlow
    }

    fun getVolumeMicControlEnabledFlow(): Flow<Boolean> {
        return isMicVolumeControlEnabled
    }

    fun muteMic(mute: Boolean) {
        Log.d(LOG_TAG, "mic muted: $mute")
        micOffFlow.value = mute
        audioManager.isMicrophoneMute = mute
    }

    fun setMicVolumeControlEnabled(enabled: Boolean) {
        isMicVolumeControlEnabled.value = enabled
    }

    fun toggleMicVolumeControlEnabled() {
        setMicVolumeControlEnabled(!isMicVolumeControlEnabled.value)
    }

    fun handleVolumeChanges(action: ControlAction): Boolean {
        if (isMicVolumeControlEnabled.value) {
            when (action) {
                MUTE_CONTROL_ACTION -> {
                    muteMic(true)
                    return true
                }
                UNMUTE_CONTROL_ACTION -> {
                    muteMic(false)
                    return true
                }
                else -> {
                    // ignore
                }
            }
        }
        return false
    }

    private fun startControlsService() {
        MicControlsService.start(appContext)
    }
}
