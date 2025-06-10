package com.example.playlistmaker.data.interactor

import android.content.Context
import android.content.res.Resources
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactor.SupportInteractor

class SupportInteractorImpl(
    private val resources: Resources,
    private val context: Context
) : SupportInteractor {

    override fun getShareIntentText() = "https://practicum.yandex.ru/android-developer/"

    override fun getSupportEmailIntentData(): Pair<String, String> {
        return "solovevrus1993@gmail.com" to resources.getString(R.string.support_subject)
    }

    override fun getTermsIntentUrl() = "https://yandex.ru/legal/practicum_offer/"
}
