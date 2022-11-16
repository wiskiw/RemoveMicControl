package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationMicVolumeControlEnabledReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicVolCtrlRcvr"
    }

    private val micControlRepository: MicControlRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        micControlRepository.onMicVolumeControlEnabledToggled()
    }
}
