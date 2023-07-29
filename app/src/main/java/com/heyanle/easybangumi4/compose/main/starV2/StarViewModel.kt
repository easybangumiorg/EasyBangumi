package com.heyanle.easybangumi4.compose.main.starV2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/7/29 22:36.
 * https://github.com/heyanLE
 */
class StarViewModel : ViewModel() {

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        val starCount: Int = -1,
        val curDictionary: String = "",
        val data: Map<String, List<CartoonStar>>,
        val selection: Set<CartoonStar> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    sealed class DialogState {
        data class ChangeUpdate(
            val selection: Set<CartoonStar>,
        ) : DialogState()

        data class Delete(
            val selection: Set<CartoonStar>,
        ) : DialogState()
    }

    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()

    private val _stateFlow = MutableStateFlow(State(data = emptyMap()))
    val stateFlow = _stateFlow.asStateFlow()


    // 最后一个选择的，用于长按区间反选
    private var lastSelectCartoon: CartoonStar? = null

    init {
        // 处理搜索
        viewModelScope.launch {
            combine(
                cartoonStarDao.flowAll(),
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
            ) { starList, searchKey ->
                if (searchKey.isNullOrEmpty()) {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            data = starList.toMap()
                        )
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            data = starList.filter { it.matches(searchKey) }.toMap()
                        )
                    }
                }
            }.collect()
        }
    }

    // 搜索
    fun onSearch(searchQuery: String?) {
        _stateFlow.update { it.copy(searchQuery = searchQuery) }
    }

    // 多选
    fun onSelectionExit() {
        _stateFlow.update {
            it.copy(selection = emptySet())
        }
    }

    fun onSelectionChange(cartoonStar: CartoonStar) {
        _stateFlow.update {
            lastSelectCartoon = cartoonStar
            val selection = if (it.selection.contains(cartoonStar)) {
                it.selection.minus(cartoonStar)
            } else it.selection.plus(cartoonStar)
            it.copy(selection = selection)
        }
    }

    fun onSelectionLongPress(cartoonStar: CartoonStar) {
        if (lastSelectCartoon?.dictionary == cartoonStar.dictionary) {
            _stateFlow.update {
                val selection = it.selection.toMutableSet()
                val lastList = it.data[cartoonStar.dictionary] ?: listOf()
                val a = lastList.indexOf(lastSelectCartoon)
                val b = lastList.indexOf(cartoonStar)
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
        } else {
            // 如果和上一个不在同一个收藏夹就走普通点击逻辑
            onSelectionChange(cartoonStar)
        }
    }

    // dialog
    fun dialogDeleteSelection() {
        _stateFlow.update {
            val selection = it.selection
            it.copy(dialog = DialogState.Delete(selection))
        }
    }

    fun dialogChangeUpdate() {
        com.heyanle.easybangumi4.utils.TODO("修改更新策略")
        _stateFlow.update {
            val selection = it.selection
            it.copy(dialog = DialogState.ChangeUpdate(selection))
        }
    }

    fun dialogDismiss() {
        _stateFlow.update {
            it.copy(dialog = null)
        }
    }

    private fun List<CartoonStar>.toMap(): Map<String, List<CartoonStar>> {
        val map = hashMapOf<String, ArrayList<CartoonStar>>()
        forEach {
            val l = map[it.dictionary] ?: arrayListOf()
            l.add(it)
            map[it.dictionary] = l
        }
        return map
    }

}