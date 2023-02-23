package com.heyanle.easybangumi.ui.home.star

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.ui.home.history.AnimHistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/1/9 21:28.
 * https://github.com/heyanLE
 */
class AnimStarViewModel : ViewModel() {

    companion object {
        private val isRefresh = MutableStateFlow<Boolean>(false)
        fun refresh() {
            isRefresh.compareAndSet(false, true)
            //isRefresh.postValue(true)
        }
    }

    val curPager = mutableStateOf(getPager().flow.cachedIn(viewModelScope))

    private val observer = Observer<Boolean> { t ->
        if (t) {
            viewModelScope.launch {
                curPager.value = getPager().flow.cachedIn(viewModelScope)
            }
        }
    }

    fun refresh() {
        curPager.value = getPager().flow.cachedIn(viewModelScope)
    }

    suspend fun onLaunch() {
        isRefresh.collectLatest {
            if (it) {
                refresh()
            }
            isRefresh.emit(false)
        }
    }

    private fun getPager(): Pager<Int, BangumiStar> {
        return Pager(
            PagingConfig(pageSize = 10)
        ) {
            EasyDB.database.bangumiStar.getAll()
        }
    }

    fun deleteBangumiStar(bangumiStar: BangumiStar) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                EasyDB.database.bangumiStarDao()
                    .deleteByBangumiSummary(
                        bangumiStar.bangumiId,
                        bangumiStar.source,
                        bangumiStar.detailUrl
                    )
            }
        }
    }
}