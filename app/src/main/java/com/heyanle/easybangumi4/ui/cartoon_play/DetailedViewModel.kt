package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.isChild
import com.heyanle.easybangumi4.cartoon.play.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.cartoon.tags.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.tags.isInner
import com.heyanle.easybangumi4.getter.CartoonInfoGetter
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by heyanlin on 2023/11/27.
 */
class DetailedViewModel(
    private val cartoonSummary: CartoonSummary,
) : ViewModel() {

    companion object {
        const val SORT_DEFAULT_KEY = "default"
    }


    val sortByDefault: SortBy<Episode> = SortBy<Episode>(
        SORT_DEFAULT_KEY,
        stringRes(com.heyanle.easy_i18n.R.string.default_word)
    ) { o1, o2 ->
        o1.order - o2.order
    }

    val sortByLabel: SortBy<Episode> = SortBy<Episode>(
        "label",
        stringRes(com.heyanle.easy_i18n.R.string.name_word)
    ) { o1, o2 ->
        o1.label.compareTo(o2.label)
    }

    val sortList = listOf(sortByDefault, sortByLabel)

    private val cartoonInfoGetter: CartoonInfoGetter by Injekt.injectLazy()
    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()
    private val cartoonTagsController: CartoonTagsController by Injekt.injectLazy()

    data class DetailedState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val throwable: Throwable? = null,
        val detail: CartoonInfo? = null,
        val playLine: List<PlayLine> = emptyList(),
        val isShowPlayLine: Boolean = true,
        val currentSortKey: String = SORT_DEFAULT_KEY,
        val isReverse: Boolean = false,
        val isStar: Boolean = false,
        val playLineWrappers: List<PlayLineWrapper> = emptyList(),
    )

    data class StarDialogState(
        val cartoon: CartoonInfo,
        val playLines: List<PlayLineWrapper>,
        val tagList: List<CartoonTag>,
    )

    var starDialogState by mutableStateOf<StarDialogState?>(null)


    private val _stateFlow = MutableStateFlow<DetailedState>(DetailedState())
    val stateFlow = _stateFlow.asStateFlow()

    val sortState = SortState<Episode>(
        sortList,
        stateFlow.map { it.currentSortKey }
            .stateIn(viewModelScope, SharingStarted.Lazily, SORT_DEFAULT_KEY),
        stateFlow.map { it.isReverse }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    )


    init {

        // 和收藏联动
        viewModelScope.launch() {
            combine(
                stateFlow.map { it.detail }.filterIsInstance<CartoonInfo>().distinctUntilChanged()
                    .stateIn(viewModelScope),
                stateFlow.map { it.playLine }.distinctUntilChanged().stateIn(viewModelScope)
            ) { info, playLines ->
                val starInfo = withContext(Dispatchers.IO) {
                    val cartoonStar = cartoonStarDao.getByCartoonSummary(
                        info.id,
                        info.source,
                        info.url
                    )

                    cartoonStar?.let { star ->
                        val nStar =
                            CartoonStar.fromCartoonInfo(info, playLines)
                        cartoonStarDao.update(
                            nStar.copy(
                                watchProcess = star.watchProcess,
                                reversal = star.reversal,
                                createTime = star.createTime,
                                sortByKey = star.sortByKey,
                                tags = star.tags,
                                isUpdate = false
                            )
                        )
                    }
                    cartoonStar
                }

                val key = if(sortList.find { it.id == starInfo?.sortByKey } == null){
                    SORT_DEFAULT_KEY
                }else starInfo?.sortByKey?:""
                _stateFlow.update {
                    it.copy(
                        currentSortKey = key,
                        isReverse = starInfo?.reversal ?: false,
                        isStar = starInfo != null
                    )
                }
            }.collect()
        }

        // 排序
        viewModelScope.launch {
            combine(
                stateFlow.map { it.playLine }.distinctUntilChanged().stateIn(viewModelScope),
                stateFlow.map { it.currentSortKey }.distinctUntilChanged().stateIn(viewModelScope),
                stateFlow.map { it.isReverse }.distinctUntilChanged().stateIn(viewModelScope),
            ) { playLines, sortKey, isReverse ->
                val sort = sortList.find { it.id == sortKey } ?: sortByDefault
                val playLineWrappers = playLines.map {
                    PlayLineWrapper(
                        it,
                        isReverse,
                        { true },
                        sort.comparator
                    )
                }
                _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        playLineWrappers = playLineWrappers,
                    )
                }
            }.collect()
        }
    }

    fun load() {
        viewModelScope.launch {
            _stateFlow.update {
                it.copy(isLoading = true)
            }
            cartoonInfoGetter.awaitCartoonInfoWithPlayLines(
                cartoonSummary.id,
                cartoonSummary.source,
                cartoonSummary.url
            ).onOK { pair ->
                _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        detail = pair.first,
                        playLine = pair.second,
                        isShowPlayLine = pair.first.isShowLine
                    )
                }
            }.onError { er ->
                _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMsg = er.errorMsg,
                        throwable = er.throwable
                    )
                }
            }
        }
    }

    fun setCartoonStar(isStar: Boolean, cartoon: CartoonInfo, playLines: List<PlayLineWrapper>) {
        viewModelScope.launch {
            if (isStar) {
                val tl = cartoonTagsController.tagsList.first()
                if (tl.find { !it.isInner() } != null) {
                    starDialogState = StarDialogState(cartoon, playLines, tl)
                } else {
                    innerStarCartoon(cartoon, playLines, emptyList())
                }

            } else {
                withContext(Dispatchers.IO) {
                    cartoonStarDao
                        .deleteByCartoonSummary(
                            cartoon.id,
                            cartoon.source,
                            cartoon.url
                        )
                }
                // AnimStarViewModel.refresh()
                if (cartoonSummary.isChild(cartoon)) {
                    _stateFlow.update {
                        it.copy(
                            isStar = false
                        )
                    }
                }
            }
        }
    }


    fun starCartoon(
        cartoon: CartoonInfo,
        playLines: List<PlayLineWrapper>,
        tag: List<CartoonTag>
    ) {
        viewModelScope.launch {
            innerStarCartoon(cartoon, playLines, tag)
        }
    }

    private suspend fun innerStarCartoon(
        cartoon: CartoonInfo,
        playLines: List<PlayLineWrapper>,
        tag: List<CartoonTag>
    ) {
        withContext(Dispatchers.IO) {
            cartoonStarDao.modify(
                CartoonStar.fromCartoonInfo(
                    cartoon,
                    playLines.map { it.playLine },
                    tag.joinToString(", ") { it.id.toString() }).apply {
                    reversal = stateFlow.value.isReverse
                    sortByKey = stateFlow.value.currentSortKey
                })
        }
        // AnimStarViewModel.refresh()
        if (cartoonSummary.isChild(cartoon)) {
            _stateFlow.update {
                it.copy(
                    isStar = true
                )
            }
        }
    }

    fun setCartoonSort(
        sortByKey: String,
        isReverse: Boolean,
        cartoon: CartoonInfo,
        isStar: Boolean
    ) {

        _stateFlow.update {
            it.copy(
                currentSortKey = sortByKey,
                isReverse = isReverse
            )
        }
        if (isStar) {
            viewModelScope.launch(Dispatchers.IO) {
                val cartoonStar = cartoonStarDao.getByCartoonSummary(
                    cartoon.id,
                    cartoon.source,
                    cartoon.url
                )

                cartoonStar?.let { star ->
                    cartoonStarDao.update(
                        star.copy(
                            reversal = isReverse,
                            sortByKey = sortByKey
                        )
                    )
                }
            }
        }
    }

}


class DetailedViewModelFactory(
    private val cartoonSummary: CartoonSummary,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailedViewModel::class.java))
            return DetailedViewModel(cartoonSummary) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}