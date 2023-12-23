package com.heyanle.easybangumi4.ui.search_migrate.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.cartoon.repository.db.dao.SearchHistoryDao
import com.heyanle.easybangumi4.utils.ViewModelOwnerMap
import com.heyanle.injekt.core.Injekt
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/12/18.
 */
class SearchViewModel(
    defSearchWord: String,
): ViewModel() {

    // 展示在 toolbar 上的文字，不一定是真正搜索的 key
    val searchBarText = mutableStateOf(defSearchWord)

    // 真正搜索的 keyword
    private val _searchFlow = MutableStateFlow(defSearchWord)
    val searchFlow = _searchFlow.asStateFlow()

    private val searchHistoryDao: SearchHistoryDao by Injekt.injectLazy()

    // 搜索历史
    val searchHistory = searchHistoryDao.flowTopContent().distinctUntilChanged()

    // 是否是搜索聚合模式
    private var isGatherOkkv by okkv<Boolean>("isGather", def = false)
    var isGather = mutableStateOf<Boolean>(isGatherOkkv)
        private set

    // viewModelOwnerMap
    val viewModelOwnerMap = ViewModelOwnerMap<String>()

    fun onGatherChange(isGather: Boolean){
        this.isGather.value = isGather
        isGatherOkkv = isGather
    }

    fun search(keyword: String){
        searchBarText.value = keyword
        _searchFlow.update {
            keyword
        }
        if(keyword.isNotEmpty()){
            addHistory(keyword)
        }
    }

    private fun addHistory(keyword: String){
        viewModelScope.launch {
            searchHistoryDao.insertOrModify(keyword)
        }
    }

    fun clearHistory(){
        viewModelScope.launch {
            searchHistoryDao.clear()
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
        fun newViewModel(defSearchKey: String): SearchViewModel {
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