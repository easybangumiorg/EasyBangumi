package org.easybangumi.next.shared.compose.home.history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.easybangumi.next.lib.utils.CoroutineProvider
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.data.CartoonInfoCase
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.preference.MainPreference
import org.koin.core.component.inject

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class HistoryVM: StateViewModel<HistoryVM.HistoryState>(HistoryState()) {

    private val mainPreference: MainPreference by inject()
    private val privateModePref = mainPreference.privateMode

    private val cartoonInfoCase: CartoonInfoCase by inject()

    data class HistoryState(
        val isLoading: Boolean = true,
        var history: List<CartoonInfo> = emptyList(),
        var searchKey: String? = null,
        var isInPrivate: Boolean = false,
        var selection: Set<CartoonInfo> = emptySet(),
        var dialog: Dialog? = null,
    )

    sealed class Dialog {
        data class Delete(
            val selection: Set<CartoonInfo>,
        ) : Dialog()

        object Clear : Dialog()
    }

    private var lastSelectHistory: CartoonInfo? = null

    init {
        viewModelScope.launch {
            privateModePref.flow().distinctUntilChanged().collectLatest {
                update { state ->
                    state.copy(
                        isInPrivate = it,
                    )
                }
            }
        }
        viewModelScope.launch {
            // 搜索和加载
            combine(
                cartoonInfoCase.flowHistory().distinctUntilChanged(),
                state.map { it.searchKey }.distinctUntilChanged(),
            ) { data, key ->
                if (key.isNullOrEmpty()) {
                    update {
                        it.copy(isLoading = false, history = data)
                    }
                } else {
                    update { state ->
                        state.copy(isLoading = false, history = data.filter { it.matches(key) })
                    }
                }
            }.collect()
        }


    }

    // 搜索

    fun search(keyword: String?) {
        update {
            it.copy(searchKey = keyword)
        }
    }

    fun exitSearch() {
        search(null)
    }

    // 多选
    fun onSelectionChange(cartoonHistory: CartoonInfo) {
        lastSelectHistory = cartoonHistory
        update {
            val selection = if (it.selection.contains(cartoonHistory)) {
                it.selection.minus(cartoonHistory)
            } else it.selection.plus(cartoonHistory)
            if (selection.isEmpty()) {
                lastSelectHistory = null
            }
            it.copy(selection = selection)
        }
    }

    fun onSelectAll() {
        update {
            it.copy(
                selection = it.history.toSet()
            )
        }
    }

    fun onSelectInvert() {
        update {
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

    fun onSelectionLongPress(cartoonHistory: CartoonInfo) {
        if (lastSelectHistory == null || lastSelectHistory == cartoonHistory) {
            onSelectionChange(cartoonHistory)
            return
        }
        update {
            val selection = it.selection.toMutableSet()
            val lastList = it.history
            var a = lastList.indexOf(lastSelectHistory)
            val b = lastList.indexOf(cartoonHistory)
            if (b > a) {
                a += 1
            } else if (a > b) {
                a -= 1
            }
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
        lastSelectHistory = cartoonHistory
    }

    fun onSelectionExit() {
        lastSelectHistory = null
        update {
            it.copy(selection = emptySet())
        }
    }

    // dialog

    fun dialogDeleteSelection() {
        update {
            val selection = it.selection
            it.copy(dialog = Dialog.Delete(selection))
        }
    }

    fun dialogDeleteOne(cartoonHistory: CartoonInfo) {
       update {
            it.copy(selection = emptySet(), dialog = Dialog.Delete(setOf(cartoonHistory)))
        }
    }

    fun clearDialog() {
        update {
            it.copy(dialog = Dialog.Clear)
        }
    }

    fun dialogDismiss() {
        update {
            it.copy(dialog = null)
        }
    }

    // 数据操作

    fun delete(cartoonHistory: CartoonInfo) {
        viewModelScope.launch {
            withContext(coroutineProvider.io()) {
//                cartoonInfoDao.deleteHistory(cartoonHistory)
            }
        }
    }

    fun delete(cartoonHistory: List<CartoonInfo>) {
        viewModelScope.launch {
            withContext(coroutineProvider.io()) {
//                cartoonInfoDao.deleteHistory(cartoonHistory)
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            withContext(coroutineProvider.io()) {
//                cartoonInfoDao.clearHistory()
            }
        }
    }


}