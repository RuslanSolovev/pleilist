package com.example.playlistmaker.di


import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.repository.AudioPlayerImpl
import com.example.playlistmaker.data.repository.LikeRepositoryImpl

import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.domain.interactor.ThemeInteractorImpl

import com.example.playlistmaker.domain.repository.ThemeRepository
import com.example.playlistmaker.domain.usecase.GetThemeUseCase
import com.example.playlistmaker.domain.usecase.SetThemeUseCase

import com.example.playlistmaker.domain.repository.AudioPlayer
import com.example.playlistmaker.domain.repositories.LikeRepository
import com.example.playlistmaker.domain.usecases.ToggleLikeUseCase
import com.example.playlistmaker.presentation.player.MediaPlayerController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideLikeRepository(sharedPreferences: SharedPreferences): LikeRepository {
        return LikeRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideThemeRepository(sharedPreferences: SharedPreferences): ThemeRepository {
        return ThemeRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideToggleLikeUseCase(repository: LikeRepository): ToggleLikeUseCase {
        return ToggleLikeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetThemeUseCase(interactor: ThemeInteractor): GetThemeUseCase {
        return GetThemeUseCase(interactor)
    }


    @Provides
    @Singleton
    fun provideSetThemeUseCase(interactor: ThemeInteractor): SetThemeUseCase {
        return SetThemeUseCase(interactor)
    }


    @Provides
    @Singleton
    fun provideAudioPlayer(impl: AudioPlayerImpl): AudioPlayer {
        return impl
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class DomainModule {

        @Binds
        abstract fun bindThemeInteractor(
            impl: ThemeInteractorImpl
        ): ThemeInteractor
    }


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