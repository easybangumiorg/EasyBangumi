package com.heyanle.easybangumi4.ui.home.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.entity.CartoonStar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/18 19:24.
 * https://github.com/heyanLE
 */
class StarViewModel : ViewModel() {

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        val starCartoonList: List<CartoonStar> = emptyList(),
        val selection: Set<CartoonStar> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow = _stateFlow.asStateFlow()

    // TODO: 过滤器弹窗状态
    sealed class DialogState {
        data class ChangeUpdate(
            val selection: Set<CartoonStar>,
        ) : DialogState()

        data class Delete(
            val selection: Set<CartoonStar>,
        ) : DialogState()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // 搜索处理
            combine(
                stateFlow.map { it.searchQuery }.distinctUntilChanged(),
                getCartoonStarFlow(),
            ) { searchQuery: String?, starList: List<CartoonStar> ->
                if (searchQuery.isNullOrEmpty()) {
                    starList
                } else {
                    starList.filter {
                        it.matches(searchQuery)
                    }
                }
            }.collectLatest { list ->
                _stateFlow.update {
                    it.copy(starCartoonList = list, isLoading = false)
                }
            }
        }
    }

    fun onSearch(searchQuery: String?) {
        _stateFlow.update { it.copy(searchQuery = searchQuery) }
    }

    fun onSelectionAll() {
        _stateFlow.update { it.copy(selection = it.starCartoonList.toSet()) }
    }

    fun onSelectionInvert() {
        _stateFlow.update { state ->
            state.copy(selection = state.starCartoonList.filter {
                !state.selection.contains(it)
            }.toSet())
        }
    }

    fun onSelectionExit() {
        _stateFlow.update {
            it.copy(selection = emptySet())
        }
    }

    fun onSelectionChange(cartoonStar: CartoonStar) {
        _stateFlow.update {
            val selection = if (it.selection.contains(cartoonStar)) {
                it.selection.minus(cartoonStar)
            } else it.selection.plus(cartoonStar)
            it.copy(selection = selection)
        }
    }

    fun onDeleteSelection() {
        _stateFlow.update {
            val selection = it.selection
            it.copy(selection = emptySet(), dialog = DialogState.Delete(selection))
        }
    }

    fun onChangeSelectionUpdate() {
        _stateFlow.update {
            val selection = it.selection
            it.copy(selection = emptySet(), dialog = DialogState.ChangeUpdate(selection))
        }
    }

    fun onUpdate() {
        com.heyanle.easybangumi4.utils.TODO("番剧更新")
    }

    private fun getCartoonStarFlow(): Flow<List<CartoonStar>> {
        return DB.cartoonStar.flowAll()
    }


}