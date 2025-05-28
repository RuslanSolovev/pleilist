package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.mapper.toDomain
import com.example.playlistmaker.data.dto.ItunesSearchResponse
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.mapper.toDomain
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchRepositoryImpl(
    private val apiService: ItunesApiService
) : SearchRepository {

    override suspend fun searchTracks(term: String): List<Track> {
        val response = apiService.search(term)
        if (response.isSuccessful) {
            val body: ItunesSearchResponse? = response.body()
            return body
                ?.results
                ?.map(TrackDto::toDomain)
                ?: emptyList()
        }
        return emptyList()
    }

    companion object {
        fun create(): SearchRepositoryImpl {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service = retrofit.create(ItunesApiService::class.java)
            return SearchRepositoryImpl(service)
        }
    }
}
