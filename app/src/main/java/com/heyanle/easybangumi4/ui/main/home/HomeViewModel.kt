package com.heyanle.easybangumi4.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.easybangumi4.source.SourceMaster
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/25 14:39.
 * https://github.com/heyanLE
 */
class HomeViewModel : ViewModel() {

    private var selectionKeyOkkv by okkv("home_selection_key", "")

    private val _stateFlow = MutableStateFlow(HomeState(selectionKey = selectionKeyOkkv))
    val stateFlow = _stateFlow.asStateFlow()


    data class HomeState(
        val isLoading: Boolean = true,
        val pages: List<SourcePage> = emptyList(),
        val selectionIndex: Int = 0,
        //val selectionPage: SourcePage? = null,
        val topAppBarTitle: String = "",
        val selectionKey: String,
    )


    init {
        viewModelScope.launch {
            combine(
                SourceMaster.animSourceFlow,
                _stateFlow.map { it.selectionKey }.distinctUntilChanged()
            ) { sourceBundle, s ->
                val pages = sourceBundle.pages()
                if (pages.isEmpty()) {
                    null
                } else sourceBundle.page(s) ?: pages[0]
            }.collectLatest { pa ->
                if (pa == null) {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            pages = emptyList(),
                            selectionKey = "",
                            topAppBarTitle = ""
                        )
                    }
                } else {
                    selectionKeyOkkv = pa.source.key
                    var index = -1
                    val pages = pa.getPages()
                    for (i in pages.indices) {
                        if (!pages[i].newScreen) {
                            index = i
                            break
                        }
                    }
                    _stateFlow.update {
                        val realIndex =
                            if (it.selectionIndex >= 0 && it.selectionIndex < it.pages.size && !it.pages[it.selectionIndex].newScreen)
                                it.selectionIndex else index
                        it.copy(
                            isLoading = false,
                            pages = pages,
                            selectionKey = pa.source.key,
                            topAppBarTitle = pa.source.label,
                            selectionIndex = realIndex
                        )
                    }
                }
            }
        }

//        viewModelScope.launch {
//            combine(
//                _stateFlow.map { it.pages }.distinctUntilChanged(),
//                _stateFlow.map { it.selectionIndex }.distinctUntilChanged()
//            ) { list, index ->
//                if (index >= 0 && index < list.size && !list[index].newScreen) {
//                    index to list[index]
//                } else {
//                    var ii = -1
//                    val pages = list
//                    for (i in pages.indices) {
//                        if (!pages[i].newScreen) {
//                            ii = i
//                            break
//                        }
//                    }
//                    ii to if (ii == -1) null else list[ii]
//                }
//            }.collectLatest { p ->
//                _stateFlow.update {
//                    it.copy(
//                        selectionIndex = p.first,
//                        selectionPage = p.second
//                    )
//                }
//            }
//        }

    }

    fun changeSelectionPage(index: Int) {
        _stateFlow.update {
            it.copy(
                selectionIndex = index
            )
        }
    }

    fun changeSelectionSource(key: String) {
        _stateFlow.update {
            it.copy(
                selectionKey = key
            )
        }
    }

    private val viewModelOwnerStore = hashMapOf<SourcePage, ViewModelStore>()

    fun getViewModelStoreOwner(page: SourcePage) = object : ViewModelStoreOwner {

        override val viewModelStore: ViewModelStore
            get() {
                var viewModelStore = viewModelOwnerStore[page]
                if (viewModelStore == null) {
                    viewModelStore = ViewModelStore()
                    viewModelOwnerStore[page] = viewModelStore
                }
                return viewModelStore
            }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelOwnerStore.iterator().forEach {
            it.value.clear()
        }
        viewModelOwnerStore.clear()
    }


}