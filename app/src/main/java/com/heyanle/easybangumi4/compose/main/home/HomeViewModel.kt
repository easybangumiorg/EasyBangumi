package com.heyanle.easybangumi4.compose.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.component.page.PageComponent
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.injekt.core.Injekt
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

    private val sourceController: SourceLibraryController by Injekt.injectLazy()


    data class HomeState(
        val isLoading: Boolean = true,
        val pages: List<SourcePage> = emptyList(),
        val selectionIndex: Int = 0,
        //val selectionPage: SourcePage? = null,
        val isShowLabel: Boolean = true,
        val topAppBarTitle: String = "",
        val selectionKey: String,
    )


    init {
        viewModelScope.launch {
            combine(
                sourceController.sourceBundleFlow,
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
                            if (it.selectionIndex >= 0 && it.selectionIndex < pages.size && !pages[it.selectionIndex].newScreen)
                                it.selectionIndex else index
                        it.copy(
                            isLoading = false,
                            pages = pages,
                            selectionKey = pa.source.key,
                            isShowLabel = pages !is PageComponent.NonLabelSinglePage,
                            topAppBarTitle = pa.source.label,
                            selectionIndex = realIndex
                        )
                    }
                }
            }
        }

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
            if(it.selectionKey != key){
                it.copy(
                    selectionKey = key,
                    selectionIndex = -1,

                )
            }else{
                it
            }

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