package dev.wiskiw.bluetoothmiccontol.service

import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import dev.wiskiw.bluetoothmiccontol.App

class MediaControlsWatcher : MediaSessionCompat.Callback() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.Watcher"
    }

    override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
        Log.d(LOG_TAG, "onCommand: '$command'")
        super.onCommand(command, args, cb)
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        val key: KeyEvent? = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        if (key != null && key.action == KeyEvent.ACTION_DOWN) {
            Log.d(LOG_TAG, "onMediaButtonEvent")
            // Calling my method
            return true
        }
        return super.onMediaButtonEvent(mediaButtonIntent)
    }

}
