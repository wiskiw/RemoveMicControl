package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wiskiw.bluetoothmiccontol.App
import org.koin.core.component.KoinComponent

class DismissMicControlsNotificationReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.DisServRcvr"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val controlsHandlerServiceIntent = Intent(context, MicControlsService::class.java)
        context.stopService(controlsHandlerServiceIntent)
    }
}
