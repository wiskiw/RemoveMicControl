package dev.wiskiw.bluetoothmiccontol.di

import android.content.Context
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import dev.wiskiw.bluetoothmiccontol.data.repository.MicControlUseCase
import dev.wiskiw.bluetoothmiccontol.service.ControlActionHandler
import dev.wiskiw.bluetoothmiccontol.ui.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MediaSessionCompat(androidContext(), "BluetoothMicControlMediaSession") }
    single { androidContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    single<MicControlUseCase> { MicControlUseCase(get()) }
//    single<ControlActionHandler> { get() }
}

val viewModel = module {
    viewModel { MainViewModel(get()) }
}
