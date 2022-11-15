package dev.wiskiw.bluetoothmiccontol.service

import dev.wiskiw.bluetoothmiccontol.data.model.ControlAction

interface ControlActionHandler {

    fun handleAction(action: ControlAction)
}
