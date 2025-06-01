// data/interactor/SupportInteractorImpl.kt
package com.example.playlistmaker.domain.interactor

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.example.playlistmaker.R

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SupportInteractorImpl @Inject constructor(
    private val resources: Resources,
    @ApplicationContext private val context: Context
) : SupportInteractor {

    override fun getShareIntentText(): String {
        return "https://practicum.yandex.ru/android-developer/"
    }

    override fun getSupportEmailIntentData(): Pair<Intent, String> {
        val email = "solovevrus1993@gmail.com"
        val subject = resources.getString(R.string.soobchenie_razrab)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        return Pair(intent, resources.getString(R.string.no_email_client_message))
    }

    override fun getTermsIntentUrl(): String {
        return "https://yandex.ru/legal/practicum_offer/"
    }
}