package com.heyanle.easybangumi4.ui.search_migrate.search.gather

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
import com.heyanle.easybangumi4.ui.search_migrate.search.SearchRequest
import com.heyanle.easybangumi4.ui.search_migrate.search.SubmittedSearchRequestGate
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/12/18.
 */
class GatherSearchViewModel(
    searchComponents: List<SearchComponent>
): ViewModel() {

    data class GatherSearchItem(
        val searchComponent: SearchComponent,
        val flow: Flow<PagingData<CartoonCover>>
    )

    // 当前搜索的关键字，用于刷新和懒加载判断
    var curKeyWord: String = ""

    private var searchComponents = searchComponents
    private val requestGate = SubmittedSearchRequestGate()

    private val _searchItemList = MutableStateFlow<List<GatherSearchItem>?>(emptyList())
    val searchItemList = _searchItemList.asStateFlow()

    val webViewHelperV2Impl: WebViewHelperV2Impl by Inject.injectLazy()

    /** A verification result is valid for exactly one source request. */
    private val verificationTemp = hashMapOf<VerificationRequestKey, VerificationResult>()
    private val verificationInProgress = hashSetOf<VerificationRequestKey>()

    val verificationProvider: (sourceKey: String, key: Int, keyword: String) -> VerificationResult? = { sourceKey, key, keyword ->
        verificationTemp.remove(VerificationRequestKey(sourceKey, keyword, key))
    }

    /**
     * Consumes a submitted request exactly once.
     *
     * A search destination can leave composition while a detail page is on top, but its
     * ViewModel and cached Paging flows remain alive. Re-consuming the same request on return
     * would replace those flows and make restored results flash empty. A newer sequence still
     * deliberately recreates the Pagers, including when the keyword itself is unchanged.
     */
    fun submitSearch(request: SearchRequest) {
        if (!requestGate.shouldHandle(request, _searchItemList.value != null)) return
        replaceSearch(request.keyword)
    }

    /**
     * Reconciles asynchronously supplied source bundles without recreating Pagers that still
     * belong to the same SearchComponent instance.
     */
    fun updateSearchComponents(components: List<SearchComponent>) {
        if (searchComponents.map { it.source.key } == components.map { it.source.key } &&
            searchComponents.zip(components).all { (old, new) -> old === new }
        ) {
            return
        }
        searchComponents = components
        if (curKeyWord.isEmpty() || _searchItemList.value == null) return

        val existing = _searchItemList.value.orEmpty()
            .associateBy { it.searchComponent.source.key }
        _searchItemList.value = components.map { component ->
            existing[component.source.key]
                ?.takeIf { it.searchComponent === component }
                ?: GatherSearchItem(
                    component,
                    getPager(curKeyWord, component).flow.cachedIn(viewModelScope),
                )
        }
    }

    fun newSearchKey(searchKey: String, force: Boolean = false) {
        if (!force && curKeyWord == searchKey && _searchItemList.value != null) return
        replaceSearch(searchKey)
    }

    private fun replaceSearch(searchKey: String) {
        if (searchKey.isEmpty()) {
            curKeyWord = ""
            _searchItemList.value = null
            return
        }
        curKeyWord = searchKey
        _searchItemList.value = searchComponents.map {
            GatherSearchItem(
                it,
                getPager(searchKey, it).flow.cachedIn(viewModelScope)
            )
        }
    }

    fun onSearchNeedWebCheck(
        sourceKey: String,
        searchNeedWebViewCheckBusinessException: SearchNeedVerificationBusinessException,
        onRetry: () -> Unit
    ){
        viewModelScope.launch {
            val request = searchNeedWebViewCheckBusinessException.request
            val requestKey = VerificationRequestKey(sourceKey, request.keyword, request.key)
            // A card may be visible in both the All and source tabs during recomposition.
            // Only let its owning source start one verification flow at a time.
            if (!verificationInProgress.add(requestKey)) return@launch
            try {
                verificationTemp[requestKey] = VerificationHelper.start(
                    searchNeedWebViewCheckBusinessException.verificationParam,
                    webViewHelperV2Impl,
                )
                // This retry belongs to the PagingSource that emitted the exception. The
                // shared paging flow then updates both All and the matching source tab.
                onRetry()
            } finally {
                verificationInProgress.remove(requestKey)
            }
        }

    }

    override fun onCleared() {
        verificationTemp.clear()
        verificationInProgress.clear()
        super.onCleared()
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


}

private data class VerificationRequestKey(
    val sourceKey: String,
    val keyword: String,
    val pageKey: Int,
)

class GatherSearchViewModelFactory(
    private val searchComponents: List<SearchComponent>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GatherSearchViewModel::class.java))
            return GatherSearchViewModel(searchComponents) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}
