package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.interactor.*
import com.example.playlistmaker.data.repository.*
import com.example.playlistmaker.domain.interactor.*
import com.example.playlistmaker.domain.repositories.LikeRepository
import com.example.playlistmaker.domain.repository.*
import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase
import com.example.playlistmaker.domain.util.TimeFormatter
import com.example.playlistmaker.presentation.SearchViewModel
import com.example.playlistmaker.presentation.viewmodel.*
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(ItunesApiService::class.java)
    }
}

val repositoryModule = module {
    single<SearchRepository> { SearchRepositoryImpl(get()) }

    single<SharedPreferences> {
        androidContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(
            prefs = get(),
            gson = get()
        )
    }

    single<LikeRepository> { LikeRepositoryImpl(get()) }

    single<MediaPlayer> { MediaPlayer() } // Добавлено создание MediaPlayer

    single<AudioPlayer> { AudioPlayerImpl(get()) } // Передаем MediaPlayer в конструктор

    single { Gson() }
}

val interactorModule = module {
    single<SearchInteractor> { SearchInteractorImpl(get()) }
    single<HistoryInteractor> { HistoryInteractorImpl(get()) }
    single<ThemeInteractor> { ThemeInteractorImpl(get()) }
    single<SupportInteractor> {
        SupportInteractorImpl(
            resources = androidContext().resources,
            context = androidContext()
        )
    }

    single<AudioPlayerInteractor> {
        AudioPlayerInteractor(get())
    }

    single<MediaPlayerInteractor> {
        MediaPlayerInteractor(get(), get())
    }

    single { ToggleLikeUseCase(get()) }
    single { TimeFormatter }
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { MediaViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}

val appModules = listOf(
    networkModule,
    repositoryModule,
    interactorModule,
    viewModelModule
)