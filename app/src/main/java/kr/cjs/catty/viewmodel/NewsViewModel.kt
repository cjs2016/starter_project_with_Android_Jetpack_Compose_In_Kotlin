package kr.cjs.catty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.room.util.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.cjs.catty.view.NewsItem
import kr.cjs.catty.view.RetrofitClient
import kr.cjs.catty.view.RetrofitRepository

class NewsViewModel: ViewModel() {

    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList

    private val repository: RetrofitRepository = RetrofitClient.repository

    private val currentQuery = MutableStateFlow("가필드고양이")

    fun naverFetchNews(query: String){
        viewModelScope.launch {
            try {
                val response = repository.naverFetchNews(query)
                _newsList.value = response.items
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val newsPagingData: Flow<PagingData<NewsItem>> = currentQuery.flatMapLatest { query ->
        repository.getNewsPagingData(query)
    }.cachedIn(viewModelScope)

    fun searchNews(query: String){
        currentQuery.value = query
    }
}