package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.example.playlistmaker.data.repository.AudioPlayerImpl
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.LikeRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl

import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.interactor.ThemeInteractorImpl
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.HistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractorImpl
import com.example.playlistmaker.domain.interactor.SupportInteractor
import com.example.playlistmaker.domain.interactor.SupportInteractorImpl
import com.example.playlistmaker.domain.interactor.ThemeInteractor

import com.example.playlistmaker.domain.repositories.LikeRepository
import com.example.playlistmaker.domain.repository.AudioPlayer
import com.example.playlistmaker.domain.repository.HistoryRepository

import com.example.playlistmaker.domain.repository.SearchRepository

import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase
import com.example.playlistmaker.presentation.player.MediaPlayerController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Network
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideItunesApiService(retrofit: Retrofit): ItunesApiService {
        return retrofit.create(ItunesApiService::class.java)
    }


    // SharedPreferences
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    // Repositories
    @Provides
    @Singleton
    fun provideSearchRepository(apiService: ItunesApiService): SearchRepository {
        return SearchRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(sharedPreferences: SharedPreferences): HistoryRepository {
        return HistoryRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideLikeRepository(sharedPreferences: SharedPreferences): LikeRepository {
        return LikeRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(): AudioPlayer {
        return AudioPlayerImpl()
    }


    // Use Cases
    @Provides
    @Singleton
    fun provideToggleLikeUseCase(likeRepository: LikeRepository): ToggleLikeUseCase {
        return ToggleLikeUseCase(likeRepository)
    }

    // Resources
    @Provides
    @Singleton
    fun provideResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    // MediaPlayerController
    @Provides
    @Singleton
    fun provideMediaPlayerController(
        audioPlayer: AudioPlayer
    ): MediaPlayerController {
        return MediaPlayerController(
            audioPlayer = audioPlayer,
            trackUrl = "",
            onTimeUpdate = {},
            onPlayStateChanged = {},
            onLikeStateChanged = {},
            onError = {}
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindSearchInteractor(
        impl: SearchInteractorImpl
    ): SearchInteractor

    @Binds
    abstract fun bindHistoryInteractor(
        impl: HistoryInteractorImpl
    ): HistoryInteractor

    @Binds
    abstract fun bindThemeInteractor(
        impl: ThemeInteractorImpl
    ): ThemeInteractor

    @Binds
    abstract fun bindSupportInteractor(
        impl: SupportInteractorImpl
    ): SupportInteractor
}