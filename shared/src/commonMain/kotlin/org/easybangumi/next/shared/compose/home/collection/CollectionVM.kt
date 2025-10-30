package org.easybangumi.next.shared.compose.home.collection

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.data.room.cartoon.dao.CartoonInfoDao
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.koin.core.component.inject
import kotlin.toString

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
class CollectionVM: StateViewModel<CollectionVM.State>(State()) {

    private val cartoonInfoDao: CartoonInfoDao by inject()

    // TODO 接入真正的数据
    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        val starCount: Int = 0,
        val curTab: CartoonTag? = null,
        val tagList: List<CartoonTag> = emptyList(),
        val data: Map<String, List<CartoonInfo>> = emptyMap(),
        val selection: Set<CartoonInfo> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    ) {
        val isFilter = curTab?.isInFilter ?: false
    }

    sealed class DialogState {}

    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonInfo? = null
    private var lastSelectTag: CartoonTag? = null

    // 删除
    fun fireDelete(selection: Set<CartoonInfo>) {}
    // 修改分类
    fun fireChangeTag(selection: Set<CartoonInfo>, tags: List<CartoonTag>) {}
    // 迁移
    fun fireMigrate(selection: Set<CartoonInfo>) {}
    // 更新
    fun fireUpdate(selection: Set<CartoonInfo>) {}


    // 多选 =============

    // 退出多选
    fun onSelectionExit() {
        lastSelectCartoon = null
        lastSelectTag = null
        update {
            it.copy(selection = emptySet())
        }
    }

    // 全选
    fun onSelectAll() {
        update {
            val dd = it.data[it.curTab?.label] ?: emptyList()
            it.copy(
                selection = it.selection.plus(dd)
            )
        }
    }

    // 反选
    fun onSelectInvert() {
        update {
            val dd = it.data[it.curTab?.label] ?: emptyList()
            val selection = it.selection.toMutableSet()
            dd.forEach { star ->
                if (selection.contains(star)) {
                    selection.remove(star)
                } else {
                    selection.add(star)
                }
            }
            it.copy(
                selection = selection
            )
        }
    }

    // 点击（选中/取消选中）
    fun onSelectionChange(cartoon: CartoonInfo) {
        lastSelectCartoon = cartoon
        lastSelectTag = state.value.curTab
        update {
            val selection = if (it.selection.contains(cartoon)) {
                it.selection.minus(cartoon)
            } else it.selection.plus(cartoon)
            it.copy(selection = selection)
        }
        if (state.value.selection.isEmpty()) {
            lastSelectCartoon = null
            lastSelectTag = null
        }
    }

    // 长按 - 最后一个到长按中间反选
    fun onSelectionLongPress(cartoonInfo: CartoonInfo) {
        if (lastSelectCartoon != null && lastSelectTag != null && lastSelectTag == state.value.curTab) {
            update {
                val selection = it.selection.toMutableSet()
                val lastList = it.data[lastSelectTag?.label] ?: listOf()
                var a = lastList.indexOf(lastSelectCartoon)
                val b = lastList.indexOf(cartoonInfo)
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
            lastSelectCartoon = cartoonInfo
            lastSelectTag = state.value.curTab
        } else {
            // 如果和上一个不在同一个收藏夹就走普通点击逻辑
            onSelectionChange(cartoonInfo)
        }
    }



    fun fireUpdateSelection() {

    }
    // dialog =====
    fun dialogDeleteSelection() {
//        _stateFlow.update {
//            val selection = it.selection
//            it.copy(dialog = com.heyanle.easybangumi4.ui.main.star.StarViewModel.DialogState.Delete(selection))
//        }
    }


    fun dialogChangeTag() {
//        _stateFlow.update {
//            it.copy(dialog = com.heyanle.easybangumi4.ui.main.star.StarViewModel.DialogState.ChangeTag(it.selection, it.tagList.filter { ! it.isInner }))
//        }
    }

    fun dialogMigrateSelect() {
//        _stateFlow.update {
//            it.copy(dialog = com.heyanle.easybangumi4.ui.main.star.StarViewModel.DialogState.MigrateSource(it.selection))
//        }
    }

    fun dialogProc() {
//        _stateFlow.update {
//            it.copy(
//                dialog = com.heyanle.easybangumi4.ui.main.star.StarViewModel.DialogState.Proc
//            )
//        }
    }

}