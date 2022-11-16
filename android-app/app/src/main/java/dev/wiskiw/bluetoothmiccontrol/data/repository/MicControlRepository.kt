package dev.wiskiw.bluetoothmiccontrol.data.repository

import android.content.Context
import android.media.AudioManager
import android.util.Log
import dev.wiskiw.bluetoothmiccontrol.App
import dev.wiskiw.bluetoothmiccontrol.data.model.ChangeVolumeDirection
import dev.wiskiw.bluetoothmiccontrol.service.MicControlsService
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

    private val isUserInCommunicationFlow = MutableStateFlow(audioManager.mode == AudioManager.MODE_IN_COMMUNICATION)
    private val isMicMutedFlow = MutableStateFlow(audioManager.isMicrophoneMute)
    private val isMicVolumeControlEnabledFlow = MutableStateFlow(isUserInCommunicationFlow.value)

    private var lastChangeVolumeDirection: ChangeVolumeDirection? = null

    init {
        isUserInCommunicationFlow
            .onEach { isUserInCommunication ->
                // automatically enable VolumeButton Mic controls when user in communication
                isMicVolumeControlEnabledFlow.value = isUserInCommunication

                if (isUserInCommunication) {
                    // start service to be sure it's running
                    startService()
                } else {
                    audioManager.isMicrophoneMute = false
                    isMicMutedFlow.value = audioManager.isMicrophoneMute
                }
            }
            .launchIn(repositoryScope)

        isMicVolumeControlEnabledFlow
            .onEach { lastChangeVolumeDirection = null }
            .launchIn(repositoryScope)

        // start service for the first time
        startService()
    }

    fun getIsMicMutedFlowFlow(): Flow<Boolean> {
        return isMicMutedFlow
    }

    fun getIsUserInCommunicationFlow(): Flow<Boolean> {
        return isUserInCommunicationFlow
    }

    fun getIsVolumeMicControlEnabledFlow(): Flow<Boolean> {
        return isMicVolumeControlEnabledFlow
    }

    fun setMicMuted(mute: Boolean) {
        Log.d(LOG_TAG, "set mic muted: $mute")
        isMicMutedFlow.value = mute
        audioManager.isMicrophoneMute = mute
    }

    fun setMicVolumeControlEnabled(enabled: Boolean) {
        isMicVolumeControlEnabledFlow.value = enabled
    }

    private fun startService() {
        val startServiceIntent = MicControlsService.getStartIntent(appContext)
        appContext.startForegroundService(startServiceIntent)
    }

    fun onMicVolumeControlEnabledToggled() {
        setMicVolumeControlEnabled(!isMicVolumeControlEnabledFlow.value)
    }

    fun onMicMuteToggled() {
        setMicMuted(!isMicMutedFlow.value)
    }

    fun onScoAudioStateChanged(state: Int) {
        when (state) {
            AudioManager.SCO_AUDIO_STATE_CONNECTING, AudioManager.SCO_AUDIO_STATE_CONNECTED ->
                isUserInCommunicationFlow.value = true

            AudioManager.SCO_AUDIO_STATE_DISCONNECTED, AudioManager.SCO_AUDIO_STATE_ERROR ->
                isUserInCommunicationFlow.value = false

            else -> {
                isUserInCommunicationFlow.value = audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
            }
        }
    }

    fun onScoVolumeChanged(old: Int, new: Int): Boolean {
        val direction = when {
            old < new -> ChangeVolumeDirection.UP
            old > new -> ChangeVolumeDirection.DOWN
            else -> null
        }

        if (isMicVolumeControlEnabledFlow.value) {
            if (lastChangeVolumeDirection == null || direction != lastChangeVolumeDirection) {
                when (direction) {
                    ChangeVolumeDirection.UP -> setMicMuted(false)
                    ChangeVolumeDirection.DOWN -> setMicMuted(true)
                    else -> {
                        // ignore
                    }
                }
                lastChangeVolumeDirection = direction
                return true
            }
        }

        lastChangeVolumeDirection = direction
        return false
    }
}
