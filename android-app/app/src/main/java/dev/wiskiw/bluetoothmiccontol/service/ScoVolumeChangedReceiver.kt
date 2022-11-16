package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScoVolumeChangedReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.VolumeRcvr"

        private const val STREAM_BLUETOOTH_SCO = 6
    }

    private val audioManager: AudioManager by inject()
    private val micControlRepository: MicControlRepository by inject()

    private var ignoreNext = false
    private var previousVolume: Int = 0

    init {
        saveVolume(getVolume())
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // todo fix edge cases

        if (ignoreNext) {
            ignoreNext = false
            saveVolume(getVolume())
            return
        }

        val old = previousVolume
        val new = getVolume()

        val isRollbackRequired = onValueChangedByUser(old, new)
        saveVolume(new)

        if (isRollbackRequired) {
            ignoreNext = true
            setVolume(old)
        }
    }

    private fun onValueChangedByUser(old: Int, new: Int): Boolean {
        Log.d(LOG_TAG, "volume change by user to $new")

        return micControlRepository.onScoVolumeChanged(old, new)
    }

    private fun saveVolume(level: Int) {
        previousVolume = level
    }

    private fun setVolume(level: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            audioManager.setStreamVolume(STREAM_BLUETOOTH_SCO, level, 0)
        }, 50)
    }

    private fun getVolume(): Int {
        return audioManager.getStreamVolume(STREAM_BLUETOOTH_SCO)
    }
}
