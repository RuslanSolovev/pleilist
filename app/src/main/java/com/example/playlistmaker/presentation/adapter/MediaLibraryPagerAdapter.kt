package com.example.playlistmaker.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.presentation.fragments.FavoritesFragment

import com.example.playlistmaker.presentation.ui.fragments.PlaylistsFragment

class MediaLibraryPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoritesFragment()
            1 -> PlaylistsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}