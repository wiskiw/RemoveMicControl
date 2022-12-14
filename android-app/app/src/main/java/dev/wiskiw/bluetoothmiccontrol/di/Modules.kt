package dev.wiskiw.bluetoothmiccontrol.di

import android.content.Context
import android.media.AudioManager
import dev.wiskiw.bluetoothmiccontrol.data.repository.MicControlRepository
import dev.wiskiw.bluetoothmiccontrol.service.NotificationChannelService
import dev.wiskiw.bluetoothmiccontrol.ui.main.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { androidContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    single { MicControlRepository(androidApplication(), get()) }
    single { NotificationChannelService(androidApplication()) }
}

val viewModel = module {
    viewModel { MainViewModel(get()) }
}
