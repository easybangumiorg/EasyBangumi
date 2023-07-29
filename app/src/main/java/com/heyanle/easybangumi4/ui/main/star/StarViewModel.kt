package com.heyanle.easybangumi4.ui.main.star

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.base.entity.CartoonStar
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
 * Created by HeYanLe on 2023/3/18 19:24.
 * https://github.com/heyanLE
 */
class StarViewModel : ViewModel() {

    private val allPager = getAllPager().flow.cachedIn(viewModelScope)

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String? = null,
        var starCount: Int = -1,
        var pager: Flow<PagingData<CartoonStar>>,
        val selection: Set<CartoonStar> = setOf(),
        val hasActiveFilters: Boolean = false,
        val dialog: DialogState? = null
    )

    private val _stateFlow = MutableStateFlow(State(pager = allPager))
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
        viewModelScope.launch {
            // 搜索处理
            stateFlow.map { it.searchQuery }.distinctUntilChanged().collectLatest { key ->
                if (key.isNullOrEmpty()) {
                    val count = withContext(Dispatchers.IO) { DB.cartoonStar.countAll() }
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            pager = allPager,
                            starCount = count)
                    }
                } else {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            pager = getSearchPager(key).flow,)
                    }
                }
            }
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
            val selection = if (it.selection.contains(cartoonStar)) {
                it.selection.minus(cartoonStar)
            } else it.selection.plus(cartoonStar)
            it.copy(selection = selection)
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

    // 数据更新

    fun onUpdate() {
        com.heyanle.easybangumi4.utils.TODO("番剧更新")
    }

    fun delete(list: List<CartoonStar>) {
        viewModelScope.launch (Dispatchers.IO) {
            DB.cartoonStar.delete(list)
        }

    }

    // 内部

    private fun getAllPager(): Pager<Int, CartoonStar> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            DB.cartoonStar.pageAll()
        }
    }

    private fun getSearchPager(keyword: String): Pager<Int, CartoonStar> {
        return Pager(
            PagingConfig(pageSize = 50)
        ) {
            DB.cartoonStar.pageSearch(keyword)
        }
    }


}