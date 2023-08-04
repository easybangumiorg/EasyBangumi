package com.heyanle.easybangumi4.compose.search.searchpage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.toIdentify
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.base.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/27 22:57.
 * https://github.com/heyanLE
 */
class SearchPageViewModel(
    private val searchComponent: SearchComponent
) : ViewModel() {

    var curKeyWord: String = ""
    val searchPagingState = mutableStateOf<Flow<PagingData<CartoonCover>>?>(null)

    private fun getPager(keyword: String): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = searchComponent.getFirstSearchKey(keyword)
        ) {
            PagingSearchSource(searchComponent, keyword)
        }
    }

}

class SearchPageViewModelFactory(
    private val searchComponent: SearchComponent
) : ViewModelProvider.Factory {

    companion object {

        @Composable
        fun newViewModel(searchComponent: SearchComponent): SearchPageViewModel {
            return viewModel(factory = SearchPageViewModelFactory(searchComponent))
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchPageViewModel::class.java))
            return SearchPageViewModel(searchComponent) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}