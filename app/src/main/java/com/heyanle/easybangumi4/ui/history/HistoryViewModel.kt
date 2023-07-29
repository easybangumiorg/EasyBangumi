package com.heyanle.easybangumi4.ui.history

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.base.entity.CartoonHistory
import com.heyanle.easybangumi4.preferences.InPrivatePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/16 22:14.
 * https://github.com/heyanLE
 */
class HistoryViewModel : ViewModel() {

    val lazyListState = LazyListState(0, 0)

    data class HistoryState(
        var pager: Flow<PagingData<CartoonHistory>>,
        var searchKey: String? = null,
        var isInPrivate: Boolean = InPrivatePreferences.stateFlow.value,
        var selection: Set<CartoonHistory> = emptySet(),
        var dialog: Dialog? = null,
    )

    sealed class Dialog {
        data class Delete(
            val selection: Set<CartoonHistory>,
        ) : Dialog()

        object Clear : Dialog()
    }

    private val allPager = getAllPager().flow.cachedIn(viewModelScope)

    private val _stateFlow = MutableStateFlow(HistoryState(allPager))
    val stateFlow = _stateFlow.asStateFlow()


    init {
        viewModelScope.launch {
            // 搜索处理
            stateFlow.map { it.searchKey }.distinctUntilChanged().collectLatest { key ->
                if (key.isNullOrEmpty()) {
                    _stateFlow.update {
                        it.copy(pager = allPager)
                    }
                } else {
                    _stateFlow.update {
                        it.copy(pager = getSearchPager(key).flow)
                    }
                }
            }
        }

        // 无痕模式
        viewModelScope.launch {
            InPrivatePreferences.stateFlow.collectLatest { v ->
                _stateFlow.update {
                    it.copy(isInPrivate = v)
                }
            }
        }
    }

    // 搜索

    fun search(keyword: String?) {
        _stateFlow.update {
            it.copy(searchKey = keyword)
        }
    }

    fun exitSearch() {
        search(null)
    }

    // 多选
    fun onSelectionChange(cartoonHistory: CartoonHistory) {
        _stateFlow.update {
            val selection = if (it.selection.contains(cartoonHistory)) {
                it.selection.minus(cartoonHistory)
            } else it.selection.plus(cartoonHistory)
            it.copy(selection = selection)
        }
    }

    fun onSelectionExit(){
        _stateFlow.update {
            it.copy(selection = emptySet())
        }
    }

    // dialog

    fun dialogDeleteSelection() {
        _stateFlow.update {
            val selection = it.selection
            it.copy( dialog = Dialog.Delete(selection))
        }
    }

    fun dialogDeleteOne(cartoonHistory: CartoonHistory) {
        _stateFlow.update {
            it.copy(selection = emptySet(), dialog = Dialog.Delete(setOf(cartoonHistory)))
        }
    }

    fun clearDialog() {
        _stateFlow.update {
            it.copy(dialog = Dialog.Clear)
        }
    }

    fun dialogDismiss() {
        _stateFlow.update {
            it.copy(dialog = null)
        }
    }

    // 数据操作

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

    fun delete(cartoonHistory: List<CartoonHistory>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DB.cartoonHistory.delete(cartoonHistory)
            }
        }
    }

    fun  clear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DB.cartoonHistory.clear()
            }
        }
    }

    // 内部方法


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