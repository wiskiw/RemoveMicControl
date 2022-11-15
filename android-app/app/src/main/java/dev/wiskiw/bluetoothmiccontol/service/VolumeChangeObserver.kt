package dev.wiskiw.bluetoothmiccontol.service

import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.util.Log
import dev.wiskiw.bluetoothmiccontol.App

class VolumeChangeObserver(
    private val audioManager: AudioManager,
    private val streamType: Int,
    private val onChanged: (Int, Int) -> Unit,
    handler: Handler,
) : ContentObserver(handler) {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.VolumeObserv"
    }

    private var previousVolume: Int = getVolume()

    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    override fun onChange(selfChange: Boolean) {
        val currentVolume = getVolume()

        Log.d(LOG_TAG, "new volume observed: $currentVolume")

        if (previousVolume != currentVolume) {
            onChanged.invoke(previousVolume, currentVolume)
            previousVolume = currentVolume
        }
    }

    private fun getVolume(): Int {
        return audioManager.getStreamVolume(streamType)
    }
}
