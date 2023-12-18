package com.heyanle.easybangumi4.ui.source_migrate.search.gather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.ui.source_migrate.PagingSearchSource
import com.heyanle.easybangumi4.ui.source_migrate.search.normal.NormalSearchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/12/18.
 */
class GatherSearchViewModel(
    private val searchComponents: List<SearchComponent>
): ViewModel() {

    data class GatherSearchItem(
        val searchComponent: SearchComponent,
        val flow: Flow<PagingData<CartoonCover>>
    )

    // 当前搜索的关键字，用于刷新和懒加载判断
    var curKeyWord: String = ""

    private val _searchItemList = MutableStateFlow<List<GatherSearchItem>?>(emptyList())
    val searchItemList = _searchItemList.asStateFlow()

    fun newSearchKey(searchKey: String) {
        viewModelScope.launch {
            if (curKeyWord == searchKey && _searchItemList.value != null) {
                return@launch
            }
            if (searchKey.isEmpty()) {
                curKeyWord = ""
                _searchItemList.value = null
                return@launch
            }
            curKeyWord = searchKey
            _searchItemList.update {
                searchComponents.map {
                    GatherSearchItem(
                        it,
                        getPager(searchKey, it).flow.stateIn(viewModelScope)
                    )
                }
            }

        }
    }


    private fun getPager(
        keyword: String,
        searchComponent: SearchComponent
    ): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = searchComponent.getFirstSearchKey(keyword)
        ) {
            PagingSearchSource(searchComponent, keyword)
        }
    }


}

class GatherSearchViewModelFactory(
    private val searchComponents: List<SearchComponent>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GatherSearchViewModel::class.java))
            return GatherSearchViewModel(searchComponents) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}