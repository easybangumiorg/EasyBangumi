package com.heyanle.easybangumi4.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.utils.ViewModelOwnerMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/27 22:55.
 * https://github.com/heyanLE
 */
class SearchViewModel(
    defSearchKey: String,
): ViewModel() {

    val searchBarText = mutableStateOf(defSearchKey)

    private val _searchFlow = MutableStateFlow(defSearchKey)
    val searchFlow = _searchFlow.asStateFlow()

    val searchHistory = mutableStateListOf<String>()

    init {
        initSearchHistory()
    }

    private val viewModelOwnerMap = ViewModelOwnerMap<SearchComponent>()

    fun getViewModel(searchComponent: SearchComponent): ViewModelStoreOwner{
        return viewModelOwnerMap.getViewModelStoreOwner(searchComponent)
    }


    fun search(keyword: String){
        searchBarText.value = keyword
        viewModelScope.launch {
            _searchFlow.emit(keyword)

            addHistory(keyword)

            viewModelScope.launch(Dispatchers.IO){
                delay(200)
                initSearchHistory()
            }

        }

    }

    fun clearHistory(){
        viewModelScope.launch (Dispatchers.IO){
            DB.searchHistory.deleteAll()
            searchHistory.clear()
        }
    }

    private fun initSearchHistory() {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) {
                DB.searchHistory.getAllContent()
            }
            searchHistory.clear()
            searchHistory += history
        }

    }

    private fun addHistory(keyword: String) {
        if (keyword.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            DB.searchHistory.insertOrModify(keyword)
            initSearchHistory()
        }

    }

    override fun onCleared() {
        super.onCleared()
        viewModelOwnerMap.clear()
    }

}

class SearchViewModelFactory(
    private val defSearchKey: String
) : ViewModelProvider.Factory {

    companion object {

        @Composable
        fun newViewModel(defSearchKey: String): SearchViewModel{
            return viewModel<SearchViewModel>(factory = SearchViewModelFactory(defSearchKey))
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java))
            return SearchViewModel(defSearchKey) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}