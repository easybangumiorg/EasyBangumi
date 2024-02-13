package com.heyanle.easybangumi4.ui.common.page.list

import androidx.compose.runtime.mutableIntStateOf
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
 * Created by heyanlin on 2024/2/9 10:57.
 */
class SourceGroupListViewModel(
    private val pageGroup: List<SourcePage.SingleCartoonPage>
): ViewModel() {

    val pageList = pageGroup.map { it to getPager(it).flow.cachedIn(viewModelScope) }
    val selected = mutableIntStateOf(0)

    private fun getPager(listPage: SourcePage.SingleCartoonPage): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = listPage.firstKey()
        ) {
            ListPagePagingSource(listPage)
        }
    }

}

class SourceGroupListViewModelFactory(
    private val pageGroup: List<SourcePage.SingleCartoonPage>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SourceGroupListViewModel::class.java))
            return SourceGroupListViewModel(pageGroup) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}