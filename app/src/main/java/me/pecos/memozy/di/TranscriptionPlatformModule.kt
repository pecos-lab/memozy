package me.pecos.memozy.di

import me.pecos.memozy.BuildConfig
import me.pecos.memozy.platform.transcription.LiveTranscriptionService
import me.pecos.memozy.platform.transcription.provideLiveTranscriptionService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val transcriptionPlatformModule = module {
    single<LiveTranscriptionService> {
        provideLiveTranscriptionService(
            context = androidContext(),
            workerUrl = BuildConfig.WORKER_URL,
            appKey = BuildConfig.APP_SECRET_KEY,
        )
    }
}
