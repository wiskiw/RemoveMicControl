package dev.wiskiw.bluetoothmiccontrol.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import dev.wiskiw.bluetoothmiccontrol.R

class NotificationChannelService(
    private val context: Context,
) {

    companion object {
        const val SERVICE_NOTIFICATION_CHANNEL_ID = "Service"
        const val CONTROLS_NOTIFICATION_CHANNEL_ID = "MicControls"
    }

    fun setup() {
        createNotificationChannel(
            SERVICE_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.service_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        createNotificationChannel(
            CONTROLS_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.mic_control_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
    }

    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }
}
