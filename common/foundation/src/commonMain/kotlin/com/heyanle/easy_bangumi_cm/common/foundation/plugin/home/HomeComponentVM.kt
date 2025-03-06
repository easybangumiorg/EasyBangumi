package com.heyanle.easy_bangumi_cm.common.foundation.plugin.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.easy_bangumi_cm.base.utils.DataState
import com.heyanle.easy_bangumi_cm.common.foundation.view_model.ViewModelOwnerMap
import com.heyanle.easy_bangumi_cm.common.foundation.paging.SingleHomePagePagingSource
import com.heyanle.easy_bangumi_cm.common.foundation.view_model.ParentViewModel
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonCover
import com.heyanle.easy_bangumi_cm.plugin.api.base.toDataState
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2025/3/5.
 */

// ================== CartoonHomePageGroupViewModel ==================
class GroupHomePageViewModel(
    private val group: HomePage.Group,
): ParentViewModel<HomePage.SingleCartoonPage>() {

    private val _pageListFlow: MutableStateFlow<DataState<List<HomePage.SingleCartoonPage>>> = MutableStateFlow(
        DataState.none())
    val pageListFlow = _pageListFlow.asStateFlow()


    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _pageListFlow.update { DataState.loading() }
            val res = group.loadPage()
            _pageListFlow.update { res.toDataState() }
        }
    }


}
@Suppress("UNCHECKED_CAST")
class GroupHomePageViewModelFactory(
    private val group: HomePage.Group,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras
    ): T {
        if (modelClass == GroupHomePageViewModel::class) {
            return GroupHomePageViewModel(group) as T
        }
        return super.create(modelClass, extras)
    }
}


// ================== CartoonHomePageViewModel ==================
class SingleHomePageViewModel(
    private val singleCartoonPage: HomePage.SingleCartoonPage
) : ViewModel() {

    val pager = mutableStateOf(getPager().flow.cachedIn(viewModelScope))

    fun refresh() {
        pager.value = getPager().flow.cachedIn(viewModelScope)
    }

    private fun getPager(): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = singleCartoonPage.firstKey()
        ) {
            SingleHomePagePagingSource(singleCartoonPage)
        }
    }


}

@Suppress("UNCHECKED_CAST")
class SingleHomePageViewModelFactory(
    private val singleCartoonPage: HomePage.SingleCartoonPage
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras
    ): T {
        if (modelClass == SingleHomePageViewModel::class) {
            return SingleHomePageViewModel(singleCartoonPage) as T
        }
        return super.create(modelClass, extras)
    }
}