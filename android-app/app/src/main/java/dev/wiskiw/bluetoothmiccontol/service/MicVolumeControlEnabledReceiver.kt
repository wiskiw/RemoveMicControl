package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MicVolumeControlEnabledReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicVolCtrlRcvr"
    }

    private val micControlUseCase: MicControlRepository by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        micControlUseCase.toggleMicVolumeControlEnabled()
    }
}
