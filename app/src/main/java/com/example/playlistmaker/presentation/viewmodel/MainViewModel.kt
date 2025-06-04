package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeInteractor: ThemeInteractor
) : ViewModel() {

}