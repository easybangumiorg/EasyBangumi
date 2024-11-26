package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.ui.common.page.paging.ListPagePagingSource

/**
 * Created by HeYanLe on 2023/2/25 20:45.
 * https://github.com/heyanLE
 */
class SourceListViewModel(
    private val listPage: SourcePage.SingleCartoonPage
) : ViewModel() {

    val curPager = mutableStateOf(getPager().flow.cachedIn(viewModelScope))

    fun refresh() {
        curPager.value = getPager().flow.cachedIn(viewModelScope)
    }

    private fun getPager(): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = listPage.firstKey()
        ) {
            ListPagePagingSource(listPage)
        }
    }

}

class SourceListViewModelFactory(
    private val listPage: SourcePage.SingleCartoonPage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SourceListViewModel::class.java))
            return SourceListViewModel(listPage) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}