package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.HistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractorImpl

object Creator {

    fun provideSearchInteractor(context: Context): SearchInteractor {
        val repository = SearchRepositoryImpl.create()
        return SearchInteractorImpl(repository)
    }

    fun provideHistoryInteractor(context: Context): HistoryInteractor {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val repository = HistoryRepositoryImpl(sharedPreferences)
        return HistoryInteractorImpl(repository)
    }
}
