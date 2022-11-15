package dev.wiskiw.bluetoothmiccontol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MicVolumeControlToggleReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.MicToggleRcvr"
    }

    private val micControlUseCase: MicControlUseCase by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        micControlUseCase.toggleMicVolumeControl()
    }
}
