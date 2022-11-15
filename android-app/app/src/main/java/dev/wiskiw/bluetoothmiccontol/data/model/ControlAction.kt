package dev.wiskiw.bluetoothmiccontol.data.model

sealed interface ControlAction {

    data class VolumeChanged(
        val old: Int,
        val new: Int,
    ) : ControlAction {
        fun isChangedUp(): Boolean = old < new
    }

    enum class Media : ControlAction {
        STOP,
        PAUSE,
        PLAY,
        SKIP_TO_PREVIOUS,
        SKIP_TO_NEXT,
        PLAY_PAUSE,
    }
}
