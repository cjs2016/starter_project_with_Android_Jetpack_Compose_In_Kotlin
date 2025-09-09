package kr.cjs.catty.view

import android.R.attr.description
import android.R.attr.text
import androidx.core.text.HtmlCompat
import kr.cjs.catty.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.cjs.catty.view.RetrofitClient.naverNewsApi
import okio.IOException


data class NaverNewsDTO(
    val lastBuildDate: String,
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<NewsItem>
)

data class NewsItem (
    val title: String,
    val originallink: String,
    val link: String,
    val description: String,
    val pubDate: String,
)

fun decodeHtml(text: String): String {
      return androidx.core.text.HtmlCompat.fromHtml(text, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}


interface NaverNewsApi {
    // Headers are now added via an Interceptor for better security.
    @GET("v1/search/news.json")
    suspend fun searchNews(
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "sim"
    ): NaverNewsDTO
}

object RetrofitClient {
    private const val BASE_URL = "https://openapi.naver.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val naverNewsApi: NaverNewsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Use the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverNewsApi::class.java)
    }

    // Expose only the repository to the rest of the app to enforce a clean architecture.
    val repository: RetrofitRepository = RetrofitRepository(naverNewsApi)
}

class RetrofitRepository(private val retrofitApi: NaverNewsApi) {
    suspend fun naverFetchNews(query: String): NaverNewsDTO = retrofitApi.searchNews(query)

    fun getNewsPagingData(query: String): Flow<PagingData<NewsItem>>{
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
        ),
            pagingSourceFactory = { NewsPagingSource(retrofitApi,query) }
        ).flow
    }

}

private const val NAVER_API_STARTING_INDEX = 1

class NewsPagingSource(
    private val naverNewsApi: NaverNewsApi,
    private val query: String
): PagingSource<Int, NewsItem>() {

    override suspend fun  load(params: LoadParams<Int>): LoadResult<Int, NewsItem>{

        val start = params.key ?: NAVER_API_STARTING_INDEX

        return try {
            val response = naverNewsApi.searchNews(query = query, start = start, display = params.loadSize)
            val news = response.items
            LoadResult.Page(
                data = news,
                prevKey = if (start == NAVER_API_STARTING_INDEX) null else start - params.loadSize,
                nextKey = if (news.isEmpty()) null else start + params.loadSize
            )

        } catch (e: IOException){
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NewsItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)

        }
    }
}