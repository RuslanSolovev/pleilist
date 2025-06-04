package com.example.playlistmaker.domain.interactor

interface SupportInteractor {
    fun getShareIntentText(): String
    fun getSupportEmailIntentData(): Pair<String, String> // Возвращаем данные вместо Intent
    fun getTermsIntentUrl(): String
}