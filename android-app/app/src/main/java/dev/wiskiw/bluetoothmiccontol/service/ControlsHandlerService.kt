package dev.wiskiw.bluetoothmiccontol.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.R
import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject


class ControlsHandlerService : Service() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.Service"

        private const val NOTIFICATION_ID = 1338
        private const val CHANNEL_ID_MIC_CONTROLS = "MediaPlaybackService"

        private const val STREAM_BLUETOOTH_SCO = 6
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val mediaSession: MediaSessionCompat by inject()
    private val actionHandler: ControlActionHandler by inject<MicControlUseCase>()
    private val micControlUseCase: MicControlUseCase by inject<MicControlUseCase>()

    private val notificationManager: NotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private var notificationBuilder: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel(CHANNEL_ID_MIC_CONTROLS, getString(R.string.mic_control_notification_channel))

        val micControlNotification = getNotification(CHANNEL_ID_MIC_CONTROLS)
        startForeground(NOTIFICATION_ID, micControlNotification)

        observeVolumeChanges()

        micControlUseCase.getVolumeMicControlEnabledFlow()
            .onEach { isControlEnabled ->
                val notification = getNotification(CHANNEL_ID_MIC_CONTROLS, isControlEnabled)
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
            .launchIn(serviceScope)
    }

    private fun getNotification(channelId: String, isControlEnabled: Boolean? = null): Notification {
        val builder = notificationBuilder ?: NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.mic_control_notification_title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        if (isControlEnabled != null) {
            builder.clearActions()

            val buttonLabel =
                if (isControlEnabled) getString(R.string.mic_control_notification_action_disable_volume_control)
                else getString(R.string.mic_control_notification_action_enable_volume_control)

            val intent = Intent(this, MicVolumeControlToggleReceiver::class.java)
            val actionIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            builder.addAction(R.mipmap.ic_launcher_round, buttonLabel, actionIntent)
        }

        return builder.build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

    private fun observeVolumeChanges() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION")

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val volumeChangedReceiver = VolumeChangedReceiver(
            audioManager = audioManager,
            streamType = STREAM_BLUETOOTH_SCO,
            rollback = true,
            onChanged = { old, new ->
                val controlAction = when {
                    old < new -> ControlAction.VOLUME_UP
                    old > new -> ControlAction.VOLUME_DOWN
                    else -> null
                }

                controlAction?.let { actionHandler.handleAction(it) }
            }
        )
        registerReceiver(volumeChangedReceiver, intentFilter)
    }

    override fun onDestroy() {
        mediaSession.isActive = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // todo
        return null
    }
}
