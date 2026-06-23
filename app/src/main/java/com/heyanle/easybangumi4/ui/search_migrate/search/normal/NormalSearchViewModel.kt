package com.heyanle.easybangumi4.ui.search_migrate.search.normal

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.source.utils.VerificationHelper
import com.heyanle.easybangumi4.ui.search_migrate.PagingSearchSource
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * Created by heyanlin on 2023/12/18.
 */
class NormalSearchViewModel(
    private val searchComponent: SearchComponent,
) : ViewModel() {

    // 当前搜索的关键字，用于刷新和懒加载判断
    var curKeyWord: String = ""

    val searchPagingState = mutableStateOf<Flow<PagingData<CartoonCover>>?>(null)

    var isRefreshing = mutableStateOf(false)


    val webViewHelperV2Impl: WebViewHelperV2Impl by Inject.injectLazy()

    private val verificationTemp = hashMapOf<Pair<String, Int>, VerificationResult>()

    val verificationProvider: (key: Int, keyword: String) -> VerificationResult? = { key, keyword ->
        verificationTemp.remove(keyword to key)
    }

    fun newSearchKey(searchKey: String) {
        viewModelScope.launch {
            if (curKeyWord == searchKey) {
                return@launch
            }
            if (searchKey.isEmpty()) {
                curKeyWord = ""
                searchPagingState.value = null
                return@launch
            }
            curKeyWord = searchKey
            searchPagingState.value =
                getPager(searchKey, searchComponent).flow.cachedIn(viewModelScope)
        }
    }

    private fun getPager(
        keyword: String,
        searchComponent: SearchComponent
    ): Pager<Int, CartoonCover> {

        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = searchComponent.getFirstSearchKey(keyword)
        ) {
            PagingSearchSource(searchComponent, keyword, verificationProvider)
        }
    }

    fun onSearchNeedWebCheck(
        searchNeedWebViewCheckBusinessException: SearchNeedVerificationBusinessException,
        onRetry: () -> Unit
    ){
        viewModelScope.launch {
            val request = searchNeedWebViewCheckBusinessException.request
            verificationTemp[request.keyword to request.key] = VerificationHelper.start(
                searchNeedWebViewCheckBusinessException.verificationParam,
                webViewHelperV2Impl,
            )
            onRetry()
        }

    }

    override fun onCleared() {
        verificationTemp.clear()
        super.onCleared()
    }

}

class NormalSearchViewModelFactory(
    private val searchComponent: SearchComponent
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NormalSearchViewModel::class.java))
            return NormalSearchViewModel(searchComponent) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}
