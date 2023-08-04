package com.heyanle.easybangumi4.compose.common.page.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.heyanle.bangumi_source_api.api.component.page.SourcePage
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.base.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.compose.common.page.paging.ListPagePagingSource
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentMap

/**
 * Created by HeYanLe on 2023/2/25 20:45.
 * https://github.com/heyanLE
 */
class SourceListViewModel(
    private val listPage: SourcePage.SingleCartoonPage
) : ViewModel() {

    val curPager = mutableStateOf(getPager().flow.cachedIn(viewModelScope))

    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()
    val starFlow =
        cartoonStarDao.flowAll()
            .map {
                it.map {
                    "${it.id} ${it.source} ${it.url}"
                }.toSet()
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val curStar = mutableStateOf<CartoonCover?>(null)

    private val sourceController: SourceLibraryController by Injekt.injectLazy()

    fun refresh() {
        curPager.value = getPager().flow.cachedIn(viewModelScope)
    }

    private fun getPager(): Pager<Int, CartoonCover> {
        return Pager(
            PagingConfig(pageSize = 10),
            initialKey = listPage.firstKey()
        ) {
            ListPagePagingSource(listPage)
        }
    }

    private var lastStarJob: Job? = null
    fun longPress(cartoonCover: CartoonCover) {
        lastStarJob?.cancel()
        lastStarJob = viewModelScope.launch {
            val isStar = starFlow.value.contains("${cartoonCover.id} ${cartoonCover.source} ${cartoonCover.url}")
            if(isStar || isCoverCur(cartoonCover)){
                curStar.value = null
                lastStarJob?.cancel()
                cartoonStarDao.deleteByCartoonSummary(cartoonCover.id, cartoonCover.source, cartoonCover.url)
            }else{
                curStar.value = cartoonCover
                sourceController.sourceBundleFlow.value.detailed(cartoonCover.source)?.getAll(
                    CartoonSummary(cartoonCover.id, cartoonCover.source, cartoonCover.url)
                )?.complete {
                    if(isActive){
                        cartoonStarDao.modify(CartoonStar.fromCartoon(it.data.first, it.data.second).apply {
                            reversal = false
                        })
                        curStar.value = null
                    }

                }?.error {
                    it.throwable.printStackTrace()
                    if(isActive) {
                        curStar.value = null
                        (stringRes(com.heyanle.easy_i18n.R.string.detailed_error) + it.throwable.message).moeSnackBar()
                    }
                }
            }
        }



    }

    fun isCoverCur(cartoonCover: CartoonCover): Boolean{
        return curStar.value?.id == cartoonCover.id && curStar.value?.source == cartoonCover.source && curStar.value?.url == cartoonCover.url
    }


}

class SourceListViewModelFactory(
    private val listPage: SourcePage.SingleCartoonPage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SourceListViewModel::class.java))
            return SourceListViewModel(listPage) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}