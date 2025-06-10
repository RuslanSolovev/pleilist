import android.content.Context
import com.example.playlistmaker.data.dto.ItunesApiService
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.domain.interactor.HistoryInteractor
import com.example.playlistmaker.domain.interactor.HistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractorImpl
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: ItunesApiService by lazy {
        retrofit.create(ItunesApiService::class.java)
    }

    private val gson: Gson by lazy {
        Gson()
    }

    fun provideSearchInteractor(context: Context): SearchInteractor {
        val repository = SearchRepositoryImpl(apiService)
        return SearchInteractorImpl(repository)
    }

    fun provideHistoryInteractor(context: Context): HistoryInteractor {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val repository = HistoryRepositoryImpl(sharedPreferences, gson)
        return HistoryInteractorImpl(repository)
    }
}