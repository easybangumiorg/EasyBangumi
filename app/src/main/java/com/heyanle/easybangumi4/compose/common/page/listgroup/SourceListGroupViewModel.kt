package com.heyanle.easybangumi4.compose.common.page.listgroup

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/25 21:20.
 * https://github.com/heyanLE
 */
class SourceListGroupViewModel(
    private val listPage: SourcePage.Group
): ViewModel() {

    sealed class GroupState {
        object None: GroupState()

        object Loading: GroupState()

        class Group(val list: List<SourcePage.SingleCartoonPage>): GroupState()

        class Error(val errorMsg: String): GroupState()
    }

    var groupState by mutableStateOf<GroupState>(GroupState.None)

    fun refresh(){
        viewModelScope.launch {
            groupState = GroupState.Loading
            listPage.loadPage()
                .complete {
                    groupState = GroupState.Group(it.data)
                }
                .error {
                    it.throwable.printStackTrace()
                    groupState =
                        GroupState.Error(
                            if(it.isParserError) stringRes(com.heyanle.easy_i18n.R.string.source_error)
                            else it.throwable.message?:stringRes(com.heyanle.easy_i18n.R.string.loading_error)
                        )
                }
        }
    }

    sealed class CurListPageState {
        object None: CurListPageState()

        class Page(val pageListPage: SourcePage.SingleCartoonPage): CurListPageState()

    }
    var curListState by mutableStateOf<CurListPageState>(CurListPageState.None)

    fun changeListPage(listPage: SourcePage.SingleCartoonPage){
        curListState = CurListPageState.Page(listPage)
        Log.d("SourceGroupViewModel", "changeListPage ${listPage.label}")
    }


    private val viewModelOwnerStore = hashMapOf<SourcePage, ViewModelStore>()

    fun getViewModelStoreOwner(page: SourcePage) = object: ViewModelStoreOwner {

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
    }
}

class SourceListGroupViewModelFactory(
    private val listPage: SourcePage.Group
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SourceListGroupViewModel::class.java))
            return SourceListGroupViewModel(listPage) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}