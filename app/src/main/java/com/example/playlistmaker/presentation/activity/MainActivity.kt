package com.example.playlistmaker.presentation.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNav = findViewById(R.id.bottom_navigation)

        // Настройка навигации
        bottomNav.setupWithNavController(navController)

        // Скрываем BottomNavigationView на экране плеера и ActionBar для определенных фрагментов
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.playerFragment -> {
                    bottomNav.visibility = View.GONE
                    supportActionBar?.hide()
                }
                R.id.createPlaylistFragment -> {
                    bottomNav.visibility = View.GONE
                    supportActionBar?.hide()
                }
                else -> {
                    bottomNav.visibility = View.VISIBLE
                    supportActionBar?.show()
                }
            }
        }
    }
}