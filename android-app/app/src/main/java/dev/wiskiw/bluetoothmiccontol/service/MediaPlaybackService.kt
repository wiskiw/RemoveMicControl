package dev.wiskiw.bluetoothmiccontol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.R
import org.koin.android.ext.android.inject
import kotlin.math.pow


class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.Service"

        private const val FOREGROUND_ID = 1338
        private const val CHANNEL_ID_MIC_CONTROLS = "MediaPlaybackService"
    }

    private val mediaSession: MediaSessionCompat by inject()
    private val mediaSessionWatcher: MediaControlsWatcher by inject()

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")

        mediaSession.apply {

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            val playbackState = PlaybackStateCompat.Builder()
                .setActions(2.0.pow(22.0).toLong()) // all
//                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build()

            setPlaybackState(playbackState)

            setCallback(mediaSessionWatcher)

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)

            isActive = true
            Log.d(LOG_TAG, "Session is active")
        }

        val notification = buildMicControlsNotification()
        startForeground(FOREGROUND_ID, notification)
    }

    private fun buildMicControlsNotification(): Notification {
        createNotificationChannel(CHANNEL_ID_MIC_CONTROLS, getString(R.string.mic_controls_notification_channel))

        return NotificationCompat.Builder(this, CHANNEL_ID_MIC_CONTROLS)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line...")
            )
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
        mediaSession.isActive = false

        super.onDestroy()
    }
}