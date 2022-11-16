package dev.wiskiw.bluetoothmiccontrol.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import dev.wiskiw.bluetoothmiccontrol.R
import dev.wiskiw.bluetoothmiccontrol.App
import dev.wiskiw.bluetoothmiccontrol.MainActivity
import dev.wiskiw.bluetoothmiccontrol.data.repository.MicControlRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject


class MicControlsService : Service() {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicService"

        private const val INFO_NOTIFICATION_ID = 1337
        private const val CONTROLS_NOTIFICATION_ID = 1338

        fun getStartIntent(context: Context): Intent {
            return Intent(context, MicControlsService::class.java)
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val micControlRepository: MicControlRepository by inject()

    private val notificationManager: NotificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private var controlsNotificationBuilder: NotificationCompat.Builder? = null

    private var volumeChangedReceiver: ScoVolumeChangedReceiver? = null
    private var scoAudioStateChangedReceiver: ScoAudioStateChangedReceiver? = null

    private var micOnMediaPlayer: MediaPlayer? = null
    private var micOffMediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()

        setupSounds()

        startForeground(INFO_NOTIFICATION_ID, createServiceRunningNotification())

        observeUserInCommunication()

        observeVolumeChanges()
        observeScoAudioStateChanges()
    }

    private fun setupSounds() {
        micOnMediaPlayer = createSoundNotificationMediaPlayer(R.raw.sound_mic_on)
        micOffMediaPlayer = createSoundNotificationMediaPlayer(R.raw.sound_mic_off)

        micControlRepository.getIsMicMutedFlowFlow()
            .drop(1)
            .onEach { isMicMuted ->
                val mediaPlayer = if (isMicMuted) micOffMediaPlayer else micOnMediaPlayer
                mediaPlayer?.start()
            }
            .launchIn(serviceScope)
    }

    private fun createSoundNotificationMediaPlayer(@RawRes resId: Int): MediaPlayer {
        val mediaPlayer = MediaPlayer.create(this, resId)
        val audioAttributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        mediaPlayer.setAudioAttributes(audioAttributes)
        return mediaPlayer
    }

    private fun createServiceRunningNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationChannelService.SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_service_running)
            .setContentTitle(getString(R.string.service_running_notification_title))
            .setContentText(getString(R.string.service_running_notification_message))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.service_running_notification_message))
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun observeUserInCommunication() {
        var inCommunicationObserveScope: CoroutineScope? = null

        micControlRepository.getIsUserInCommunicationFlow()
            .onEach { isUserInCommunication ->
                if (isUserInCommunication) {
                    val newInCommunicationObserveScope = serviceScope + Job()
                    inCommunicationObserveScope = newInCommunicationObserveScope

                    combine(
                        micControlRepository.getIsVolumeMicControlEnabledFlow(),
                        micControlRepository.getIsMicMutedFlowFlow(),
                    ) { isControlEnabled, isMicMuted ->
                        val notification = createMicControlNotification(
                            isMicControlEnabled = isControlEnabled,
                            isMicMuted = isMicMuted,
                        )
                        notificationManager.notify(CONTROLS_NOTIFICATION_ID, notification)
                    }
                        .launchIn(newInCommunicationObserveScope)

                } else {
                    inCommunicationObserveScope?.cancel(CancellationException("User has stopped communication mode"))
                    notificationManager.cancel(CONTROLS_NOTIFICATION_ID)
                }
            }
            .launchIn(serviceScope)
    }

    private fun createMicControlNotification(isMicControlEnabled: Boolean, isMicMuted: Boolean): Notification {
        val builder = controlsNotificationBuilder
            ?: NotificationCompat.Builder(this, NotificationChannelService.CONTROLS_NOTIFICATION_CHANNEL_ID)

        val micControlStateText =
            if (isMicControlEnabled) getString(R.string.mic_volume_control_state_enabled)
            else getString(R.string.mic_volume_control_state_disabled)
        val contentText = getString(R.string.mic_control_notification_message, micControlStateText)
        builder.setContentText(contentText)

        val micStateText =
            if (isMicMuted) getString(R.string.mic_state_muted)
            else getString(R.string.mic_state_active)
        val titleText = getString(R.string.mic_control_notification_title, micStateText)
        builder.setContentTitle(titleText)

        val smallIconRes = if (isMicMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on
        builder.setSmallIcon(smallIconRes)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setOngoing(true)

        builder.setColorized(true)
        builder.color = getColor(R.color.notification_bg_mic_active)

        // Create an explicit intent for an Activity in your app
        val notificationIntent = Intent(this, MainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val notificationPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(notificationPendingIntent)


        builder.clearActions()

        val buttonLabel =
            if (isMicMuted) getString(R.string.mic_control_notification_action_activate_mic)
            else getString(R.string.mic_control_notification_action_mute_mic)

        val toggleIntent = Intent(this, NotificationMicMuteReceiver::class.java)
        val togglePendingIntent =
            PendingIntent.getBroadcast(this, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        builder.addAction(R.mipmap.ic_launcher_round, buttonLabel, togglePendingIntent)

        return builder.build()
    }


    private fun observeVolumeChanges() {
        volumeChangedReceiver = ScoVolumeChangedReceiver()

        val intentFilter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeChangedReceiver, intentFilter)
    }

    private fun observeScoAudioStateChanges() {
        scoAudioStateChangedReceiver = ScoAudioStateChangedReceiver()

        val intentFilter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        registerReceiver(scoAudioStateChangedReceiver, intentFilter)
    }

    override fun onDestroy() {
        serviceJob.cancel(CancellationException("Service has been stopped"))

        unregisterReceiver(volumeChangedReceiver)
        unregisterReceiver(scoAudioStateChangedReceiver)

        micOnMediaPlayer?.release()
        micOffMediaPlayer?.release()

        notificationManager.cancel(CONTROLS_NOTIFICATION_ID)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // todo
        return null
    }
}
