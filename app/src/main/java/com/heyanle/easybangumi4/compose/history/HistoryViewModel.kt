package com.heyanle.easybangumi4.compose.history

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.base.entity.CartoonHistory
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
        val isLoading: Boolean = true,
        var history: List<CartoonHistory> = emptyList(),
        var searchKey: String? = null,
        var isInPrivate: Boolean = false,
        var selection: Set<CartoonHistory> = emptySet(),
        var dialog: Dialog? = null,
    )

    sealed class Dialog {
        data class Delete(
            val selection: Set<CartoonHistory>,
        ) : Dialog()

        object Clear : Dialog()
    }

    private val settingPreferences: SettingPreferences by Injekt.injectLazy()
    private val cartoonHistoryDao: CartoonHistoryDao by Injekt.injectLazy()


    private val _stateFlow = MutableStateFlow(HistoryState())
    val stateFlow = _stateFlow.asStateFlow()

    private var lastSelectHistory: CartoonHistory? = null


    init {
        viewModelScope.launch {
            // 搜索和加载
            combine(
                cartoonHistoryDao.flowAllOrderByTime().distinctUntilChanged(),
                stateFlow.map { it.searchKey }.distinctUntilChanged(),
            ) { data, key ->
                if (key.isNullOrEmpty()) {
                    _stateFlow.update {
                        it.copy(isLoading = false, history = data)
                    }
                } else {
                    _stateFlow.update { state ->
                        state.copy(isLoading = false, history = data.filter { it.matches(key) })
                    }
                }
            }.collect()
        }

        // 无痕模式
        viewModelScope.launch {
            settingPreferences.isInPrivate.flow().distinctUntilChanged()
                .stateIn(viewModelScope).collectLatest { v ->
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

    fun onSelectAll() {
        _stateFlow.update {
            it.copy(
                selection = it.history.toSet()
            )
        }
    }

    fun onSelectInvert() {
        _stateFlow.update {
            val dd = it.history
            val selection = it.selection.toMutableSet()
            dd.forEach { history ->
                if (selection.contains(history)) {
                    selection.remove(history)
                } else {
                    selection.add(history)
                }
            }
            it.copy(
                selection = selection
            )
        }

    }

    fun onSelectionLongPress(cartoonHistory: CartoonHistory) {
        if (lastSelectHistory == null || lastSelectHistory == cartoonHistory) {
            onSelectionChange(cartoonHistory)
            return
        }
        _stateFlow.update {
            val selection = it.selection.toMutableSet()
            val lastList = it.history
            val a = lastList.indexOf(lastSelectHistory)
            val b = lastList.indexOf(cartoonHistory)
            val start = a.coerceAtMost(b)
            val end = a.coerceAtLeast(b)
            for (i in start..end) {
                if (i >= 0 && i < lastList.size) {
                    val star = lastList[i]
                    if (selection.contains(star)) {
                        selection.remove(star)
                    } else {
                        selection.add(star)
                    }
                }
            }
            it.copy(
                selection = selection
            )
        }
    }

    fun onSelectionExit() {
        _stateFlow.update {
            it.copy(selection = emptySet())
        }
    }

    // dialog

    fun dialogDeleteSelection() {
        _stateFlow.update {
            val selection = it.selection
            it.copy(dialog = Dialog.Delete(selection))
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
                cartoonHistoryDao.deleteByCartoonSummary(
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
                cartoonHistoryDao.delete(cartoonHistory)
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cartoonHistoryDao.clear()
            }
        }
    }

    // 内部方法


}