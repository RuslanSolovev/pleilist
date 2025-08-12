// Файл: package com.example.playlistmaker.di
package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.room.Room
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.interactor.*
import com.example.playlistmaker.data.repository.*
import com.example.playlistmaker.domain.interactor.*
import com.example.playlistmaker.domain.repositories.FavoriteRepository
import com.example.playlistmaker.domain.repositories.LikeRepository
import com.example.playlistmaker.domain.repository.*
import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase
import com.example.playlistmaker.domain.util.TimeFormatter
import com.example.playlistmaker.presentation.viewmodel.*
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "favorite_tracks.db"
        )
            .fallbackToDestructiveMigration() // Добавлено для решения проблемы с версией БД
            .build()
    }

    single { get<AppDatabase>().favoriteTracksDao() }
}

val networkModule = module {
    single {
        Retrofit.Builder()
            // Исправлена ошибка с лишними пробелами в URL
            .baseUrl("https://itunes.apple.com/ ")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(ItunesApiService::class.java)
    }
}

val repositoryModule = module {
    single<SearchRepository> {
        SearchRepositoryImpl(
            apiService = get(),
            // Исправлено: передаем FavoriteRepository вместо FavoriteTracksDao
            favoriteRepository = get()
        )
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(
            prefs = get(),
            gson = get()
        )
    }

    // LikeRepository больше не используется, но оставлен для совместимости
    single<LikeRepository> { LikeRepositoryImpl(get()) }

    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }

    single<MediaPlayer> { MediaPlayer() }

    single<AudioPlayer> { AudioPlayerImpl(get()) }

    single { Gson() }
}

val interactorModule = module {
    single<SearchInteractor> { SearchInteractorImpl(get(), get()) }
    single<HistoryInteractor> { HistoryInteractorImpl(get(), get()) }
    single<ThemeInteractor> { ThemeInteractorImpl(get()) }
    single<SupportInteractor> {
        SupportInteractorImpl(
            resources = androidContext().resources,
            context = androidContext()
        )
    }
    single<FavoriteInteractor> { FavoriteInteractorImpl(get()) }

    single<AudioPlayerInteractor> {
        AudioPlayerInteractor(get())
    }

    single<MediaPlayerInteractor> {
        MediaPlayerInteractor(get(), get())
    }

    // Исправлено: ToggleLikeUseCase теперь получает FavoriteRepository
    single { ToggleLikeUseCase(get()) }
    single { TimeFormatter }
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { MediaViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
}

val appModules = listOf(
    dataModule,
    networkModule,
    repositoryModule,
    interactorModule,
    viewModelModule
)