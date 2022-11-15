package dev.wiskiw.bluetoothmiccontol.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.R
import dev.wiskiw.bluetoothmiccontol.data.model.ChangeVolumeDirection
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject


class MicControlsService : Service() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicService"

        private const val NOTIFICATION_ID = 1338
        private const val NOTIFICATION_CHANNEL_ID = "MediaPlaybackService"

        private const val STREAM_BLUETOOTH_SCO = 6

        fun start(context: Context) {
            val startServiceIntent = Intent(context, MicControlsService::class.java)
            context.startForegroundService(startServiceIntent)
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val micControlRepository: MicControlRepository by inject()

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var volumeChangedReceiver: VolumeChangedReceiver? = null

    private var micOnMediaPlayer: MediaPlayer? = null
    private var micOffMediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()

        setupSounds()
        setupNotification()
        observeVolumeChanges()
    }

    private fun setupSounds() {
        micOnMediaPlayer = createNotificationMediaPlayer(R.raw.sound_mic_on)
        micOffMediaPlayer = createNotificationMediaPlayer(R.raw.sound_mic_off)

        micControlRepository.getIsMicMutedFlowFlow()
            .onEach { isMicMuted ->
                val mediaPlayer = if (isMicMuted) micOffMediaPlayer else micOnMediaPlayer
                mediaPlayer?.start()
            }
            .launchIn(serviceScope)
    }

    private fun createNotificationMediaPlayer(@RawRes resId: Int): MediaPlayer {
        val mediaPlayer = MediaPlayer.create(this, resId)
        val audioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        mediaPlayer.setAudioAttributes(audioAttributes)
        return mediaPlayer
    }

    private fun setupNotification() {
        createNotificationChannel(getString(R.string.mic_control_notification_channel))

        val micControlNotification = getNotification(
            isMicControlEnabled = false,
            isMicMuted = false,
        )
        startForeground(NOTIFICATION_ID, micControlNotification)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val isControlEnabledFlow = micControlRepository.getIsVolumeMicControlEnabledFlow()
        val isMicMutedFlow = micControlRepository.getIsMicMutedFlowFlow()
        combine(isControlEnabledFlow, isMicMutedFlow) { isControlEnabled, isMicMuted ->
            val notification = getNotification(
                isMicControlEnabled = isControlEnabled,
                isMicMuted = isMicMuted,
            )
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
            .launchIn(serviceScope)

    }

    private fun getNotification(isMicControlEnabled: Boolean, isMicMuted: Boolean): Notification {
        val builder = notificationBuilder ?: NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        val contentText =
            if (isMicControlEnabled) getString(R.string.mic_control_notification_message_enabled)
            else getString(R.string.mic_control_notification_message_disabled)
        builder.setContentText(contentText)

        val smallIconRes = if (isMicMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on
        builder.setSmallIcon(smallIconRes)

        builder.setContentTitle(getString(R.string.mic_control_notification_title))
        builder.priority = NotificationCompat.PRIORITY_HIGH


        builder.clearActions()

        val buttonLabel =
            if (isMicControlEnabled) getString(R.string.mic_control_notification_action_disable_volume_control)
            else getString(R.string.mic_control_notification_action_enable_volume_control)

        val toggleIntent = Intent(this, MicVolumeControlEnabledReceiver::class.java)
        val togglePendingIntent =
            PendingIntent.getBroadcast(this, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        builder.addAction(R.mipmap.ic_launcher_round, buttonLabel, togglePendingIntent)

        val dismissIntent = Intent(this, DismissMicControlsNotificationReceiver::class.java)
        val dismissPendingIntent =
            PendingIntent.getBroadcast(this.applicationContext, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setDeleteIntent(dismissPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannel(channelName: String) {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = Color.RED
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
    }

    private fun observeVolumeChanges() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION")

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        volumeChangedReceiver = VolumeChangedReceiver(
            audioManager = audioManager,
            streamType = STREAM_BLUETOOTH_SCO,
            onChanged = { old, new ->
                val controlAction = when {
                    old < new -> ChangeVolumeDirection.UP
                    old > new -> ChangeVolumeDirection.DOWN
                    else -> null
                }

                if (controlAction != null) {
                    val isActionHandled = micControlRepository.handleVolumeChanges(controlAction)
                    return@VolumeChangedReceiver isActionHandled
                }
                return@VolumeChangedReceiver false
            }
        )
        registerReceiver(volumeChangedReceiver, intentFilter)
    }

    override fun onDestroy() {
        serviceJob.cancel(CancellationException("Service has been stopped"))

        micOnMediaPlayer?.release()
        micOffMediaPlayer?.release()

        unregisterReceiver(volumeChangedReceiver)

        micControlRepository.setMicMuted(mute = false)
        micControlRepository.setMicVolumeControlEnabled(enabled = false)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // todo
        return null
    }
}
