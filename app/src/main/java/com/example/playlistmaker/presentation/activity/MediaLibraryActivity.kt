package com.example.playlistmaker.presentation.ui.activity

import MediaLibraryPagerAdapter
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaLibraryBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_library)

        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        val adapter = MediaLibraryPagerAdapter(this)
        viewPager.adapter = adapter

        // Настройка TabLayoutMediator с форматированием текста
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabText = when (position) {
                0 -> getString(R.string.tab_favorites)
                1 -> getString(R.string.tab_playlists)
                else -> ""
            }


            val spannableString = SpannableString(tabText)
            spannableString.setSpan(
                RelativeSizeSpan(1.3f),
                0, 1,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Устанавливаем отформатированный текст вкладки
            tab.text = spannableString
        }.attach()

        // Обработка кнопки "Назад"
        findViewById<ImageButton>(R.id.back_button).setOnClickListener { finish() }
    }
}