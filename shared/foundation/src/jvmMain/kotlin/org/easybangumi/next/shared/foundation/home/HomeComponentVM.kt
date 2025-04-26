//package org.easybangumi.next.shared.foundation.home
//
//import androidx.compose.foundation.lazy.grid.LazyGridState
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.paging.Pager
//import androidx.paging.PagingConfig
//import androidx.paging.cachedIn
//import com.heyanle.easy_bangumi_cm.base.service.system.logger
//import com.heyanle.easy_bangumi_cm.base.utils.DataState
//import com.heyanle.easy_bangumi_cm.base.utils.map
//import org.easybangumi.next.shared.foundation.ScrollableHeaderState
//import org.easybangumi.next.shared.foundation.paging.CartoonPagePagingSource
//import org.easybangumi.next.shared.foundation.view_model.ParentViewModel
//import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
//import com.heyanle.easy_bangumi_cm.plugin.api.base.toDataState
//import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.CartoonPage
//import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeContent
//import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
///**
// * Created by heyanlin on 2025/3/5.
// */
//class HomeContentViewModel(
//    homeContent: HomeContent,
//) : ParentViewModel<HomePage>() {
//
//    companion object {
//        const val TAG = "HomeContentViewModel"
//    }
//
//    data class UIState (
//        val tabState: Pair<List<String>, Int>? = null,
//        val homePage: HomePage,
//    )
//    val uiState = mutableStateOf<DataState<UIState>>(DataState.loading<UIState>())
//
//
//    data class State (
//        val homeContent: HomeContent,
//        val selectionIndex: Int = 0,
//    )
//    private val _stateFlow = MutableStateFlow(State(homeContent))
//    val stateFlow = _stateFlow.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            // state -> uiState
//            _stateFlow.collectLatest {
//                uiState.value = when (it.homeContent) {
//                    is HomeContent.Single -> {
//                        DataState.ok(UIState(tabState = null, homePage = it.homeContent.single,))
//                    }
//                    is HomeContent.Multiple -> {
//                        val pageList = it.homeContent.pageList
//                        val tabList = pageList.map { it.first }
//                        var selectIndex = it.selectionIndex
//                        if (selectIndex < 0 || selectIndex >= tabList.size) {
//                            selectIndex = 0
//                        }
//                        val homePage = pageList.getOrNull(selectIndex)?.second
//                        if (homePage == null) {
//                            DataState.empty<UIState>()
//                        } else {
//                            DataState.ok(UIState(tabState = if (homePage == null) null else tabList to selectIndex, homePage = homePage))
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    fun select(index: Int) {
//        viewModelScope.launch {
//            _stateFlow.update {
//                it.copy(selectionIndex = index)
//            }
//        }
//    }
//
//}
//
//class HomePageViewModel(
//    private val homePage: HomePage,
//) : ParentViewModel<CartoonPage>() {
//
//    companion object {
//        const val TAG = "HomePageViewModel"
//    }
//
//    data class ScrollState(
//        val lazyGridState: LazyGridState = LazyGridState(),
//        val scrollableHeaderState: ScrollableHeaderState? = null,
//    )
//
//
//
//
//
//    private val scrollableHeaderStateTemp = hashMapOf<CartoonPage, ScrollableHeaderState>()
//    private val cartoonPageLazyGridStateTemp = hashMapOf<CartoonPage, LazyGridState>()
//
//    fun getLazyGridState(cartoonPage: CartoonPage): LazyGridState {
//        return cartoonPageLazyGridStateTemp.getOrPut(cartoonPage) { LazyGridState() }
//    }
//
//    fun getScrollableHeaderState(cartoonPage: CartoonPage): ScrollableHeaderState {
//        return scrollableHeaderStateTemp.getOrPut(cartoonPage) { ScrollableHeaderState(0f, 0f, 0f) }
//    }
//
//    // ================== UI State ==================
//
//    data class UIState(
//        val tabState: Pair<List<String>, Int>?,
//        val cartoonPage: CartoonPage,
//    )
//
//
//    val uiState = mutableStateOf<DataState<UIState>>(DataState.loading())
//
//    // ================== LogicState ==================
//    sealed class State {
//        data class Single(
//            val cartoonPage: DataState<CartoonPage>
//        ) : State()
//
//        data class Group(
//            val cartoonPageList: DataState<List<Pair<String, CartoonPage>>>,
//            val selectionIndex: Int = 0,
//        ) : State()
//
//        companion object {
//            fun from(homePage: HomePage): State {
//                return when (homePage) {
//                    is HomePage.Single -> {
//                        Single(DataState.Loading())
//                    }
//
//                    is HomePage.Group -> {
//                        Group(DataState.Loading())
//                    }
//                }
//            }
//        }
//    }
//    private val _stateFlow = MutableStateFlow(State.from(homePage))
//    val stateFlow = _stateFlow.asStateFlow()
//
//    init {
//        viewModelScope.launch {
//            // state -> uiState
//            _stateFlow.collectLatest {
//                uiState.value = when (it) {
//                    is State.Single -> {
//                        it.cartoonPage.map { UIState(null, it) }
//                    }
//
//                    is State.Group -> {
//                        when (it.cartoonPageList) {
//                            is DataState.Loading -> DataState.loading()
//                            is DataState.None -> DataState.none()
//                            is DataState.Error -> DataState.error(it.cartoonPageList.errorMsg, it.cartoonPageList.throwable)
//                            is DataState.Ok -> {
//                                val list = it.cartoonPageList.data
//                                val tabList = list.map { it.first }
//                                var selectIndex = it.selectionIndex
//                                if (it.selectionIndex !in list.indices) {
//                                    selectIndex = 0
//                                }
//                                val page = list.getOrNull(selectIndex)?.second
//                                if (page == null) {
//                                    DataState.empty<UIState>()
//                                } else {
//                                    DataState.ok<UIState>(UIState(tabList to selectIndex, page))
//
//                                }
//                            }
//                        }
//                    }
//                }
//                logger.i(TAG, "uiState: $uiState")
//            }
//        }
//        refresh()
//    }
//
//    // ================== Api ==================
//
//    public fun refresh() {
//        viewModelScope.launch {
//            _stateFlow.update { State.from(homePage) }
//            cleanChildren()
//            scrollableHeaderStateTemp.clearChildren()
//            cartoonPageLazyGridStateTemp.clearChildren()
//            when (homePage) {
//                is HomePage.Group -> {
//                    val res = homePage.load()
//                    _stateFlow.update { State.Group(res.toDataState()) }
//                }
//                is HomePage.Single -> {
//                    val res = homePage.load()
//                    _stateFlow.update { State.Single(res.toDataState()) }
//                }
//            }
//        }
//    }
//
//    public fun select(index: Int) {
//        viewModelScope.launch {
//            _stateFlow.update {
//                when (it) {
//                    is State.Group -> {
//                        it.copy(selectionIndex = index)
//                    }
//                    else -> it
//                }
//            }
//        }
//    }
//
//
//}
//
//class CartoonPageViewModel(
//    private val cartoonPage: CartoonPage,
//) : ViewModel() {
//
//    val pageState = mutableStateOf(getPager().flow.cachedIn(viewModelScope))
//
//    fun refresh() {
//        pageState.value = getPager().flow.cachedIn(viewModelScope)
//    }
//
//    private fun getPager(): Pager<Int, CartoonCover> {
//        return Pager(
//            PagingConfig(pageSize = 10),
//            initialKey = cartoonPage.firstKey()
//        ) {
//            CartoonPagePagingSource(cartoonPage)
//        }
//    }
//
//}
//
//
