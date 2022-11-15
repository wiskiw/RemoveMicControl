package dev.wiskiw.bluetoothmiccontol.service

import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction

class MediaSessionCallback(
    private val actionHandler: ControlActionHandler
) : MediaSessionCompat.Callback() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MSCallback"
    }

    override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
        Log.d(LOG_TAG, "onCommand: '$command'")
        super.onCommand(command, args, cb)
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        val key: KeyEvent? = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        if (key != null && key.action == KeyEvent.ACTION_DOWN) {

            val controlAction = when (key.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> ControlAction.PLAY
                KeyEvent.KEYCODE_MEDIA_PAUSE -> ControlAction.PAUSE
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> ControlAction.PLAY_PAUSE
                KeyEvent.KEYCODE_MEDIA_STOP -> ControlAction.STOP
                KeyEvent.KEYCODE_MEDIA_NEXT -> ControlAction.SKIP_TO_NEXT
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> ControlAction.SKIP_TO_PREVIOUS
                else -> null
            }

            if (controlAction != null) {
                actionHandler.handleAction(controlAction)
                return true
            }
        }
        return super.onMediaButtonEvent(mediaButtonIntent)
    }
}
