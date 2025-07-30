package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.mapper.toDomain
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepositoryImpl(
    private val apiService: ItunesApiService
) : SearchRepository {
    override fun searchTracks(term: String): Flow<List<Track>> = flow {
        val response = apiService.search(term)
        if (response.isSuccessful) {
            emit(response.body()?.results?.map { it.toDomain() } ?: emptyList())
        } else {
            emit(emptyList())
        }
    }
}