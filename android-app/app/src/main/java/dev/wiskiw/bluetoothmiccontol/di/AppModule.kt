package dev.wiskiw.bluetoothmiccontol.di

import android.support.v4.media.session.MediaSessionCompat
import dev.wiskiw.bluetoothmiccontol.service.MediaControlsWatcher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { MediaSessionCompat(androidContext(), "BluetoothMicControlMediaSession") }
    single { MediaControlsWatcher() }
}
