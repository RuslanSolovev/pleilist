package com.example.playlistmaker.data.interactor

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactor.SupportInteractor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SupportInteractorImpl @Inject constructor(
    private val resources: Resources,
    @ApplicationContext private val context: Context
) : SupportInteractor {

    override fun getShareIntentText() = "https://practicum.yandex.ru/android-developer/"

    override fun getSupportEmailIntentData(): Pair<String, String> {
        return "solovevrus1993@gmail.com" to resources.getString(R.string.support_subject)
    }

    override fun getTermsIntentUrl() = "https://yandex.ru/legal/practicum_offer/"
}