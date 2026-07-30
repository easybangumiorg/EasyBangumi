package com.heyanle.easybangumi4.ui.search_migrate.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.cartoon.repository.db.dao.SearchHistoryDao
import com.heyanle.easybangumi4.utils.ViewModelOwnerMap
import com.heyanle.inject.core.Inject
import com.heyanle.okkv2.core.okkv
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SearchMode {
    SINGLE_SOURCE,
    BY_SOURCE,
    OVERVIEW,
}

/**
 * A submitted search. [sequence] advances even when [keyword] is unchanged, so an explicit
 * search-button press can reload stale results instead of being collapsed by StateFlow.
 */
data class SearchRequest(
    val keyword: String,
    val sequence: Long,
)

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

    private val _searchRequestFlow = MutableStateFlow(SearchRequest(defSearchWord, sequence = 0))
    val searchRequestFlow = _searchRequestFlow.asStateFlow()

    private val searchHistoryDao: SearchHistoryDao by Inject.injectLazy()

    // 搜索历史
    val searchHistory = searchHistoryDao.flowTopContent().distinctUntilChanged()

    // Retained for older entry points which still write this preference. The explicit
    // three-mode preference below is authoritative once the user has made a selection.
    private var isGatherOkkv by okkv<Boolean>("isGather", def = true)
    private var searchModeOkkv by okkv<String>("searchMode", def = "")
    private val hasLegacySearchModePreference = MMKV.defaultMMKV().containsKey("isGather")
    var searchMode = mutableStateOf(
        searchModeOkkv
            .takeIf { it.isNotBlank() }
            ?.let { runCatching { SearchMode.valueOf(it) }.getOrNull() }
            // The legacy flag is a genuine user choice only when its key exists. Its default
            // value is true, so reading it alone would make a clean install look like a user
            // who selected grouped search.
            ?: if (hasLegacySearchModePreference) {
                if (isGatherOkkv) SearchMode.BY_SOURCE else SearchMode.SINGLE_SOURCE
            } else {
                SearchMode.OVERVIEW
            }
    )
        private set

    // viewModelOwnerMap
    val viewModelOwnerMap = ViewModelOwnerMap<String>()

    fun onSearchModeChange(mode: SearchMode){
        searchMode.value = mode
        searchModeOkkv = mode.name
        isGatherOkkv = mode == SearchMode.BY_SOURCE
    }

    fun search(keyword: String){
        searchBarText.value = keyword
        _searchFlow.value = keyword
        // Do not debounce an explicit submit. Consumers use this request stream to recreate
        // their paging data even when the submitted keyword equals the current one.
        _searchRequestFlow.update { previous ->
            SearchRequest(keyword, previous.sequence + 1)
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
