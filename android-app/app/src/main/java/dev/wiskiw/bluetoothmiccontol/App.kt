package dev.wiskiw.bluetoothmiccontol

import android.app.Application
import dev.wiskiw.bluetoothmiccontol.di.appModule
import dev.wiskiw.bluetoothmiccontol.di.viewModel
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
