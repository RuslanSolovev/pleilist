package com.example.playlistmaker.domain.interactor

import android.content.Intent

interface SupportInteractor {
    fun getShareIntentText(): String
    fun getSupportEmailIntentData(): Pair<Intent, String>
    fun getTermsIntentUrl(): String
}