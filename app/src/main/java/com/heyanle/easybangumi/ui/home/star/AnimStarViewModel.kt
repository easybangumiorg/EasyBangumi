package com.heyanle.easybangumi.ui.home.star

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiStar
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/9 21:28.
 * https://github.com/heyanLE
 */
class AnimStarViewModel : ViewModel() {

    companion object {
        private val isRefresh = MutableLiveData(false)
        fun refresh() {
            isRefresh.postValue(true)
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

    init {
        isRefresh.observeForever(observer)
    }

    private fun getPager(): Pager<Int, BangumiStar> {
        return Pager(
            PagingConfig(pageSize = 10)
        ) {
            EasyDB.database.bangumiStarDao().getAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isRefresh.removeObserver(observer)
    }

}