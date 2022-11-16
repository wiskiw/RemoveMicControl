package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationMicMuteReceiver : BroadcastReceiver(), KoinComponent {

    private val micControlRepository: MicControlRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        micControlRepository.onMicMuteToggled()
    }
}
