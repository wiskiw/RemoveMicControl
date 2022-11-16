package dev.wiskiw.bluetoothmiccontrol

import android.app.Application
import dev.wiskiw.bluetoothmiccontrol.di.appModule
import dev.wiskiw.bluetoothmiccontrol.di.viewModel
import dev.wiskiw.bluetoothmiccontrol.service.NotificationChannelService
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class App : Application() {

    companion object {
        const val LOG_TAG = "BMC"
    }

    private val notificationChannelService: NotificationChannelService by inject()

    override fun onCreate() {
        super.onCreate()

        setupDi()

        notificationChannelService.setup()
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
}
