package dev.wiskiw.bluetoothmiccontol.data.repository

import android.content.Context
import android.media.AudioManager
import android.util.Log
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.model.ChangeVolumeDirection
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
    }

    private val repositoryJob = Job()
    private val repositoryScope = CoroutineScope(Dispatchers.Main + repositoryJob)

    private val isMicMutedFlow = MutableStateFlow(audioManager.isMicrophoneMute)
    private val isMicVolumeControlEnabled = MutableStateFlow(true)

    private var lastChangeVolumeDirection: ChangeVolumeDirection? = null

    init {
        isMicVolumeControlEnabled
            .onEach { isEnabled ->
                if (isEnabled) {
                    startControlsService()
                } else {
                    lastChangeVolumeDirection = null
                }
            }
            .launchIn(repositoryScope)
    }

    fun getIsMicMutedFlowFlow(): Flow<Boolean> {
        return isMicMutedFlow
    }

    fun getIsVolumeMicControlEnabledFlow(): Flow<Boolean> {
        return isMicVolumeControlEnabled
    }

    fun setMicMuted(mute: Boolean) {
        Log.d(LOG_TAG, "mic muted: $mute")
        isMicMutedFlow.value = mute
        audioManager.isMicrophoneMute = mute
    }

    fun setMicVolumeControlEnabled(enabled: Boolean) {
        isMicVolumeControlEnabled.value = enabled
    }

    fun toggleMicVolumeControlEnabled() {
        setMicVolumeControlEnabled(!isMicVolumeControlEnabled.value)
    }

    fun handleVolumeChanges(direction: ChangeVolumeDirection): Boolean {
        if (isMicVolumeControlEnabled.value && (lastChangeVolumeDirection != null && direction != lastChangeVolumeDirection)) {
            when (direction) {
                ChangeVolumeDirection.UP -> setMicMuted(false)
                ChangeVolumeDirection.DOWN -> setMicMuted(true)
            }

            lastChangeVolumeDirection = direction
            return true
        }
        lastChangeVolumeDirection = direction
        return false
    }

    private fun startControlsService() {
        MicControlsService.start(appContext)
    }
}
