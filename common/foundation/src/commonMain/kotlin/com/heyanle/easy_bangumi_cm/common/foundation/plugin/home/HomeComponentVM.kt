package com.heyanle.easy_bangumi_cm.common.foundation.plugin.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.common.foundation.paging.CartoonPagePagingSource
import com.heyanle.easy_bangumi_cm.common.foundation.plugin.home.HomePageViewModel.UIState
import com.heyanle.easy_bangumi_cm.common.foundation.view_model.ParentViewModel
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
import com.heyanle.easy_bangumi_cm.plugin.api.base.toDataState
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.CartoonPage
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2025/3/5.
 */
class HomeContentViewModel(
    homeContent: HomeContent,
) : ParentViewModel<HomePage>() {

    data class UIState (
        val isLoading: Boolean = true,
        val tabState: Pair<List<String>, Int>? = null,
        val homePage: HomePage? = null,
    ) {
        fun isEmpty(): Boolean {
            return tabState?.first?.isNotEmpty() != true && homePage == null && !isLoading
        }
    }
    val uiState = mutableStateOf(UIState())


    data class State (
        val homeContent: HomeContent,
        val selectionIndex: Int = 0,
    )
    private val _stateFlow = MutableStateFlow(State(homeContent))
    val stateFlow = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            // state -> uiState
            _stateFlow.collectLatest {
                uiState.value = when (homeContent) {
                    is HomeContent.Single -> {
                        UIState(
                            isLoading = false,
                            tabState = null,
                            homePage = homeContent.single,
                        )
                    }
                    is HomeContent.Multiple -> {
                        val pageList = homeContent.pageList
                        val tabList = pageList.map { it.first }
                        val selectIndex = it.selectionIndex
                        val homePage = pageList.getOrNull(selectIndex)?.second
                        UIState(
                            isLoading = false,
                            tabState = if (homePage == null) null else tabList to selectIndex,
                            homePage = homePage,
                        )
                    }
                }
            }
        }
    }

    fun select(index: Int) {
        viewModelScope.launch {
            _stateFlow.update {
                it.copy(selectionIndex = index)
            }
        }
    }

}

class HomePageViewModel(
    private val homePage: HomePage,
) : ParentViewModel<CartoonPage>() {

    // ================== UI State ==================

    sealed class UIState {
        data object Loading : UIState()
        data class Error(
            val errorMsg: String,
            val throwable: Throwable?,
        ) : UIState()

        data class Success(
            val tabState: Pair<List<String>, Int>?,
            val cartoonPage: CartoonPage,
        ) : UIState()

        data object Empty: UIState()

    }
    val uiState = mutableStateOf<UIState>(UIState.Loading)

    // ================== LogicState ==================
    sealed class State {
        data class Single(
            val cartoonPage: DataState<CartoonPage>
        ) : State()

        data class Group(
            val cartoonPageList: DataState<List<Pair<String, CartoonPage>>>,
            val selectionIndex: Int = 0,
        ) : State()

        companion object {
            fun from(homePage: HomePage): State {
                return when (homePage) {
                    is HomePage.Single -> {
                        Single(DataState.Loading())
                    }

                    is HomePage.Group -> {
                        Group(DataState.Loading())
                    }
                }
            }
        }
    }
    private val _stateFlow = MutableStateFlow(State.from(homePage))
    val stateFlow = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            // state -> uiState
            _stateFlow.collectLatest {
                uiState.value = when (it) {
                    is State.Single -> {
                        when (it.cartoonPage) {
                            is DataState.Loading -> UIState.Loading
                            is DataState.None -> UIState.Loading
                            is DataState.Error -> UIState.Error(it.cartoonPage.errorMsg, it.cartoonPage.throwable)
                            is DataState.Ok -> {
                                UIState.Success(null, it.cartoonPage.data)
                            }
                        }
                    }

                    is State.Group -> {
                        when (it.cartoonPageList) {
                            is DataState.Loading -> UIState.Loading
                            is DataState.None -> UIState.Loading
                            is DataState.Error -> UIState.Error(
                                it.cartoonPageList.errorMsg,
                                it.cartoonPageList.throwable
                            )
                            is DataState.Ok -> {
                                val list = it.cartoonPageList.data
                                val tabList = list.map { it.first }
                                var selectIndex = it.selectionIndex
                                if (it.selectionIndex !in list.indices) {
                                    selectIndex = 0
                                }
                                val page = list.getOrNull(selectIndex)?.second
                                if (page == null) {
                                    UIState.Empty
                                } else {
                                    UIState.Success(tabList to selectIndex, page)
                                }
                            }
                        }
                    }
                }
            }
        }
        refresh()
    }

    // ================== Api ==================

    public fun refresh() {
        viewModelScope.launch {
            _stateFlow.update { State.from(homePage) }
            cleanChildren()
            when (homePage) {
                is HomePage.Group -> {
                    val res = homePage.load()
                    _stateFlow.update { State.Group(res.toDataState()) }
                }
                is HomePage.Single -> {
                    val res = homePage.load()
                    _stateFlow.update { State.Single(res.toDataState()) }
                }
            }
        }
    }

    public fun select(index: Int) {
        viewModelScope.launch {
            _stateFlow.update {
                when (it) {
                    is State.Group -> {
                        it.copy(selectionIndex = index)
                    }
                    else -> it
                }
            }
        }
    }


}

class CartoonPageViewModel(
    private val cartoonPage: CartoonPage,
) : ViewModel() {

    val pageState = mutableStateOf(getPager().flow.cachedIn(viewModelScope))

    fun refresh() {
        pageState.value = getPager().flow.cachedIn(viewModelScope)
    }

    private fun getPager(): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = cartoonPage.firstKey()
        ) {
            CartoonPagePagingSource(cartoonPage)
        }
    }

}


