package com.heyanle.easybangumi.ui.home.history

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.ui.home.star.AnimStarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/1/9 21:51.
 * https://github.com/heyanLE
 */
class AnimHistoryViewModel : ViewModel() {
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
        isRefresh.value = false
    }

    suspend fun onPageLaunch() {
        isRefresh.collectLatest {
            if (it) {
                refresh()
            }
            isRefresh.emit(false)
        }
    }

    fun refresh() {
        curPager.value = getPager().flow.cachedIn(viewModelScope)
    }

    private fun getPager(): Pager<Int, BangumiHistory> {
        return Pager(
            PagingConfig(pageSize = 10)
        ) {
            EasyDB.database.bangumiHistory.getAllOrderByTime()
        }
    }

    fun delete(bangumiHistory: BangumiHistory) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                EasyDB.database.bangumiHistory.deleteByBangumiSummary(
                    bangumiHistory.bangumiId,
                    bangumiHistory.source,
                    bangumiHistory.detailUrl
                )
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                EasyDB.database.bangumiHistory.clear()
            }
        }
    }
}