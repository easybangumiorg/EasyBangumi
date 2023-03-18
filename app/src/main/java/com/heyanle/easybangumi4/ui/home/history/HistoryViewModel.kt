package com.heyanle.easybangumi4.ui.home.history

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.entity.CartoonHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/16 22:14.
 * https://github.com/heyanLE
 */
class HistoryViewModel : ViewModel() {

    val curPager = mutableStateOf(getAllPager().flow.cachedIn(viewModelScope))
    val searchPager = mutableStateOf<Flow<PagingData<CartoonHistory>>?>(null)


    fun refreshAll() {
        curPager.value = getAllPager().flow.cachedIn(viewModelScope)
    }

    fun search(keyword: String) {
        if (keyword.isEmpty()) {
            exitSearch()
        } else {
            searchPager.value = getSearchPager(keyword).flow.cachedIn(viewModelScope)
        }

    }

    fun exitSearch() {
        searchPager.value = null
    }

    fun delete(cartoonHistory: CartoonHistory) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DB.cartoonHistory.deleteByCartoonSummary(
                    cartoonHistory.id,
                    cartoonHistory.source,
                    cartoonHistory.url,
                )
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DB.cartoonHistory.clear()
            }
        }
    }

    private fun getAllPager(): Pager<Int, CartoonHistory> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            DB.cartoonHistory.getAllOrderByTime()
        }
    }

    private fun getSearchPager(keyword: String): Pager<Int, CartoonHistory> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            DB.cartoonHistory.getSearchOrderByTime(keyword)
        }
    }
}