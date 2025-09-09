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

        // Получаем NavController через NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Находим BottomNavigationView
        bottomNav = findViewById(R.id.bottom_navigation)

        // Настройка BottomNavigationView с NavController
        bottomNav.setupWithNavController(navController)

        // Скрываем BottomNavigationView на определенных экранах
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                // Скрываем на экране плеера
                R.id.playerFragment -> {
                    bottomNav.visibility = View.GONE
                }
                // Скрываем на экране создания плейлиста
                R.id.createPlaylistFragment -> {
                    bottomNav.visibility = View.GONE
                }
                // Скрываем на экране деталей плейлиста
                R.id.playlistDetailsFragment -> {
                    bottomNav.visibility = View.GONE
                }
                // Скрываем на экране редактирования плейлиста
                R.id.editPlaylistFragment -> {
                    bottomNav.visibility = View.GONE
                }
                // Для всех остальных экранов показываем BottomNavigationView
                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}