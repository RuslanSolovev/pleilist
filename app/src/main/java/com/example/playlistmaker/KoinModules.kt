package com.example.playlistmaker.di


import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.room.Room
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.TrackForPlaylistDao
import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.interactor.*
import com.example.playlistmaker.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.data.repository.*
import com.example.playlistmaker.domain.interactor.*
import com.example.playlistmaker.domain.repositories.FavoriteRepository
import com.example.playlistmaker.domain.repositories.LikeRepository
import com.example.playlistmaker.domain.repository.AudioPlayer
import com.example.playlistmaker.domain.repository.HistoryRepository
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.usecase.AddTrackToPlaylistUseCase
import com.example.playlistmaker.domain.usecase.CreatePlaylistUseCase
import com.example.playlistmaker.domain.usecase.DeletePlaylistUseCase
import com.example.playlistmaker.domain.usecase.GetAllPlaylistsUseCase

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
            "playlist_maker.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().favoriteTracksDao() }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().trackForPlaylistDao() }
}

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
    single<SearchRepository> {
        SearchRepositoryImpl(
            apiService = get(),
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

    single<LikeRepository> { LikeRepositoryImpl(get()) }

    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            trackForPlaylistDao = get(),
            gson = get()
        )
    }

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
    single { DeletePlaylistUseCase(get()) }
    single { CreatePlaylistUseCase(get()) }
    single { GetAllPlaylistsUseCase(get()) }
    single { ToggleLikeUseCase(get()) }
    single { TimeFormatter }
    single { AddTrackToPlaylistUseCase (get())}
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { MediaViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlayerViewModel(get()) }
    viewModel { CreatePlaylistViewModel(get(), androidContext()) }
    viewModel { PlaylistsViewModel(get(), get()) }
}

val appModules = listOf(
    dataModule,
    networkModule,
    repositoryModule,
    interactorModule,
    viewModelModule
)