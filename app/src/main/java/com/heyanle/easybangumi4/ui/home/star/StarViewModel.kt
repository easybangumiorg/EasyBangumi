package com.heyanle.easybangumi4.ui.home.star

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.entity.CartoonStar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/18 19:24.
 * https://github.com/heyanLE
 */
class StarViewModel: ViewModel() {

    var starNum by mutableStateOf(0)

    var curPager = mutableStateOf(getAllPager().flow.cachedIn(viewModelScope))
    val searchPager = mutableStateOf<Flow<PagingData<CartoonStar>>?>(null)

    fun search(keyword: String) {
        if (keyword.isEmpty()) {
            exitSearch()
        } else {
            searchPager.value = getSearchPager(keyword).flow.cachedIn(viewModelScope)
        }

    }

    fun refreshNum(){
        viewModelScope.launch {
            starNum = withContext(Dispatchers.IO){
                DB.cartoonStar.countAll()
            }
        }
    }

    fun exitSearch() {
        searchPager.value = null
    }

    fun delete(cartoonStar: CartoonStar) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DB.cartoonStar.deleteByCartoonSummary(
                    cartoonStar.id,
                    cartoonStar.source,
                    cartoonStar.url,
                )
            }
        }
    }

    private fun getAllPager(): Pager<Int, CartoonStar> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            DB.cartoonStar.getAll()
        }
    }

    private fun getSearchPager(keyword: String): Pager<Int, CartoonStar> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            DB.cartoonStar.getSearch(keyword)
        }
    }
}