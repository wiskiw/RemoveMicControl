package dev.wiskiw.bluetoothmiccontrol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import dev.wiskiw.bluetoothmiccontrol.App
import dev.wiskiw.bluetoothmiccontrol.data.repository.MicControlRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScoAudioStateChangedReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.SCORcvr"
    }

    private val micControlRepository: MicControlRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) {
            val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)

            micControlRepository.onScoAudioStateChanged(state)
        }
    }
}
