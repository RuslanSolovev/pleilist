package com.example.playlistmaker.data.dto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)
interface ItunesApiService {
    @GET("search")
    suspend fun search(@Query("term") term: String): Response<ItunesSearchResponse>
}
