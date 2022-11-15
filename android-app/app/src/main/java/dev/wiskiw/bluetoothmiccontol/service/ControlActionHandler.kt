package dev.wiskiw.bluetoothmiccontol.service

import android.util.Log
import dev.wiskiw.bluetoothmiccontol.App
import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction

class ControlActionHandler {

    companion object {
        private const val LOG_TAG = "${App.LOG_TAG}.Service"

    }

    fun handleAction(action: ControlAction) {
        Log.d(LOG_TAG, "handleAction: '$action'")
    }
}
