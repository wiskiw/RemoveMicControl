package dev.wiskiw.bluetoothmiccontol

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import dev.wiskiw.bluetoothmiccontol.di.appModule
import dev.wiskiw.bluetoothmiccontol.di.viewModel
import dev.wiskiw.bluetoothmiccontol.service.ControlsHandlerService
import dev.wiskiw.bluetoothmiccontol.service.VolumeChangedReceiver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
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
            androidLogger()
            androidContext(this@App)
            modules(
                listOf(appModule, viewModel)
            )
        }
    }

    private fun setupMediaControlsWatcher() {
        val startServiceIntent = Intent(this, ControlsHandlerService::class.java)
        startForegroundService(startServiceIntent)

        // https://stackoverflow.com/a/67796162
        val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.silence)
//        mediaPlayer.isLooping = true
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.start()
    }


}
