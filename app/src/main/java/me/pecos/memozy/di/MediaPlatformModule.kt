package me.pecos.memozy.di

import me.pecos.memozy.platform.media.AndroidAudioFileStore
import me.pecos.memozy.platform.media.AndroidMediaService
import me.pecos.memozy.platform.media.AudioFileStore
import me.pecos.memozy.platform.media.MediaService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mediaPlatformModule = module {
    single<MediaService> { AndroidMediaService(androidContext()) }
    single<AudioFileStore> { AndroidAudioFileStore(androidContext()) }
}
