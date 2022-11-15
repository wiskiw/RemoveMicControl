package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DismissControlsNotificationReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.DisServRcvr"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val controlsHandlerServiceIntent = Intent(context, ControlsHandlerService::class.java)
        context.stopService(controlsHandlerServiceIntent)
    }
}
