package dev.wiskiw.bluetoothmiccontol

import android.app.Application
import android.content.Intent
import android.media.MediaPlayer
import dev.wiskiw.bluetoothmiccontol.di.appModule
import dev.wiskiw.bluetoothmiccontol.service.MediaPlaybackService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : Application() {

    companion object {
        const val LOG_TAG = "BMC"
    }

    override fun onCreate() {
        super.onCreate()

        setupDi()
        setupMediaControlsWatcher()
    }

    private fun setupDi() {
        startKoin {
            androidContext(this@App)
            modules(
                listOf(appModule)
            )
        }
    }

    private fun setupMediaControlsWatcher() {
        val startServiceIntent = Intent(this, MediaPlaybackService::class.java)
        startForegroundService(startServiceIntent)

        // https://stackoverflow.com/a/67796162
        val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.silence)
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.start()
    }
}
