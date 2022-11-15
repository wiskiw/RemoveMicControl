package dev.wiskiw.bluetoothmiccontol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.provider.Settings
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.R
import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction
import org.koin.android.ext.android.inject
import kotlin.math.pow


class ControlsHandlerService : MediaBrowserServiceCompat() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.Service"

        private const val FOREGROUND_ID = 1338
        private const val CHANNEL_ID_MIC_CONTROLS = "MediaPlaybackService"
    }

    private val mediaSession: MediaSessionCompat by inject()
    private val actionHandler = ControlActionHandler()

    private lateinit var volumeSettingsObserver: VolumeSettingsObserver

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")

        volumeSettingsObserver = VolumeSettingsObserver(Handler(Looper.getMainLooper()))
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, volumeSettingsObserver)
        setupMediaSession()
    }

    private fun setupMediaSession() {
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(2.0.pow(22.0).toLong()) // all
//                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
//            .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
            .build()

        mediaSession.setPlaybackState(playbackState)

        mediaSession.setCallback(MediaSessionCallback())

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken

        mediaSession.isActive = true
        Log.d(LOG_TAG, "Session is active")

        val notification = buildMicControlsNotification()
        startForeground(FOREGROUND_ID, notification)
    }

    private fun buildMicControlsNotification(): Notification {
        createNotificationChannel(CHANNEL_ID_MIC_CONTROLS, getString(R.string.mic_control_notification_channel))

        return NotificationCompat.Builder(this, CHANNEL_ID_MIC_CONTROLS)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.mic_control_notification_title))
            .setContentText(getString(R.string.mic_control_notification_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return null
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(volumeSettingsObserver)
        mediaSession.isActive = false

        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
            Log.d(LOG_TAG, "onCommand: '$command'")
            super.onCommand(command, args, cb)
        }

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            val key: KeyEvent? = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            if (key != null && key.action == KeyEvent.ACTION_DOWN) {

                val controlAction = when (key.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> ControlAction.Media.PLAY
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> ControlAction.Media.PAUSE
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> ControlAction.Media.PLAY_PAUSE
//                    KeyEvent.KEYCODE_MUTE ->;
//                    KeyEvent.KEYCODE_HEADSETHOOK ->;
                    KeyEvent.KEYCODE_MEDIA_STOP -> ControlAction.Media.STOP
                    KeyEvent.KEYCODE_MEDIA_NEXT -> ControlAction.Media.SKIP_TO_NEXT
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> ControlAction.Media.SKIP_TO_PREVIOUS
//                    KeyEvent.KEYCODE_MEDIA_REWIND ->;
//                    KeyEvent.KEYCODE_MEDIA_RECORD ->;
//                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ->;
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

    private inner class VolumeSettingsObserver(handler: Handler) : ContentObserver(handler) {

        val audio: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        var previousVolume: Int = getVolume()

        override fun deliverSelfNotifications(): Boolean {
            return true
        }

        override fun onChange(selfChange: Boolean, uris: MutableCollection<Uri>, flags: Int) {
            super.onChange(selfChange, uris, flags)
        }

        override fun onChange(selfChange: Boolean) {
            val currentVolume = getVolume()

            if (previousVolume != currentVolume) {
                val controlAction = ControlAction.VolumeChanged(previousVolume, currentVolume)
                actionHandler.handleAction(controlAction)

                previousVolume = currentVolume
            }
        }

        private fun getVolume(): Int {
            return audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        }
    }
}
