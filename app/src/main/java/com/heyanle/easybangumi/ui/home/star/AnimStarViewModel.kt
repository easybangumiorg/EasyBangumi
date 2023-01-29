package com.heyanle.easybangumi.ui.home.star

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.ui.home.history.AnimHistoryViewModel
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/9 21:28.
 * https://github.com/heyanLE
 */
class AnimStarViewModel : ViewModel() {

    companion object {
        private val isRefresh = MutableLiveData(false)
        private val refresh = isRefresh.distinctUntilChanged()
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

    fun refresh(){
        curPager.value = getPager().flow.cachedIn(viewModelScope)
    }
    init {
        refresh.observeForever(observer)
    }

    private fun getPager(): Pager<Int, BangumiStar> {
        return Pager(
            PagingConfig(pageSize = 10)
        ) {
            EasyDB.database.bangumiStar.getAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        refresh.removeObserver(observer)
    }

}