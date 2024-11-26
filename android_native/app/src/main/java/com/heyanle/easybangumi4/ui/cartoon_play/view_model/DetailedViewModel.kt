package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.case.CartoonInfoCase
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class DetailedViewModel(
    private val cartoonSummary: CartoonSummary,
) : ViewModel() {

    private val cartoonInfoCase: CartoonInfoCase by Inject.injectLazy()
    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val cartoonStarController: CartoonStarController by Inject.injectLazy()
    private val settingPreferences: SettingPreferences by Inject.injectLazy()


    private val _stateFlow = MutableStateFlow<DetailState>(DetailState())
    val stateFlow = _stateFlow.asStateFlow()

    private var job: Job? = null

    val sortStateFlow = combine(
        stateFlow.map { it.cartoonInfo }.filterIsInstance<CartoonInfo>().map { it.sortByKey.ifEmpty { PlayLineWrapper.SORT_DEFAULT_KEY } }
            .stateIn(viewModelScope, SharingStarted.Lazily, PlayLineWrapper.SORT_DEFAULT_KEY),
        stateFlow.map { it.cartoonInfo }.filterIsInstance<CartoonInfo>().map { it.reversal }
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    ){sortId, isReversal ->
        SortState<Episode>(
            PlayLineWrapper.sortList,
            sortId,
            isReversal
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, SortState(PlayLineWrapper.sortList,
        PlayLineWrapper.SORT_DEFAULT_KEY, false))

    val gridCount = settingPreferences.detailedScreenEpisodeGridCount.stateIn(viewModelScope)

    fun setGridCount(count: Int){
        settingPreferences.detailedScreenEpisodeGridCount.set(count)
    }



    data class DetailState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val throwable: Throwable? = null,
        val cartoonInfo: CartoonInfo? = null,
        val starDialogState: StarDialogState? = null,
    )

    data class StarDialogState(
        val cartoon: CartoonInfo,
        val tagList: List<CartoonTag>,
    )


    init {
        load()
    }

    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            _stateFlow.update {
                it.copy(isLoading = true, cartoonInfo = null)
            }
            cartoonInfoCase.awaitCartoonInfoWithPlayLines(
                cartoonSummary.id,
                cartoonSummary.source,
            )
                .onOK {
                    yield()
                    _stateFlow.update { detail ->
                        detail.copy(
                            isLoading = false,
                            isError = false,
                            errorMsg = "",
                            throwable = null,
                            cartoonInfo = it,
                            starDialogState = null
                        )
                    }
                }
                .onError { err ->
                    yield()
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMsg = err.errorMsg,
                            throwable = err.throwable,
                            cartoonInfo = null,
                            starDialogState = null
                        )
                    }
                }
        }
    }

    fun setCartoonStar(star: Boolean, cartoon: CartoonInfo) {
        if (!cartoon.match(cartoonSummary)) {
            return
        }
        viewModelScope.launch {
            if (star) {
                val tl = cartoonStarController.cartoonTagFlow.first().tagList
                if (tl.find { !it.isInner && !it.isDefault } != null) {
                    _stateFlow.update {
                        it.copy(
                            starDialogState = StarDialogState(cartoon, tl.filter { !it.isInner }.sortedBy { it.order })
                        )
                    }
                } else {
                    cartoonInfoDao.modify(
                        cartoon.copy(
                            starTime = System.currentTimeMillis(),
                            tags = "",
                            isUpdate = false,
                        )
                    )
                    refreshFromDB()
                }
            } else {
                cartoonInfoDao.modify(
                    cartoon.copy(
                        starTime = 0,
                        tags = "",
                        isUpdate = false,
                    )
                )
                refreshFromDB()
            }
        }
    }

    fun dialogSetCartoonStar(
        cartoon: CartoonInfo,
        tag: List<CartoonTag>,
    ) {
        if (!cartoon.match(cartoonSummary)) {
            return
        }
        viewModelScope.launch {
            val cartoonInfo = cartoon.copy(
                starTime = System.currentTimeMillis(),
                tags = tag.joinToString(", ") { it.label.toString() },
                isUpdate = false,
            )
            cartoonInfoDao.modify(cartoonInfo)
            refreshFromDB()
        }
    }

    fun setCartoonSort(
        sortByKey: String,
        isReverse: Boolean,
        cartoon: CartoonInfo,
    ) {
        if (!cartoon.match(cartoonSummary)) {
            return
        }
        viewModelScope.launch {
            val cartoonInfo = cartoon.copy(
                sortByKey = sortByKey,
                reversal = isReverse,
            )
            cartoonInfoDao.modify(cartoonInfo)
            refreshFromDB()
        }
    }

    fun dialogExit() {
        viewModelScope.launch {
            _stateFlow.update {
                it.copy(
                    starDialogState = null
                )
            }
        }
    }

    private suspend fun refreshFromDB() {
        val n =
            cartoonInfoDao.getByCartoonSummary(
                cartoonSummary.id,
                cartoonSummary.source,
            )
        if (n != null && n.isDetailed) {
            _stateFlow.update {
                it.copy(
                    cartoonInfo = n
                )
            }
        } else {
            load()
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