package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class DetailedViewModel(
    private val cartoonSummary: CartoonSummary,
) : ViewModel() {

    private val cartoonRepository: CartoonRepository by Inject.injectLazy()
    private val cartoonStarController: CartoonStarController by Inject.injectLazy()
    private val settingPreferences: SettingPreferences by Inject.injectLazy()


    private val _stateFlow = MutableStateFlow<DetailState>(DetailState())
    val stateFlow = _stateFlow.asStateFlow()

    private var job: Job? = null

    val sortStateFlow = stateFlow.map { detailState ->
        val cartoonInfo = detailState.cartoonInfo
        SortState<Episode>(
            PlayLineWrapper.sortList,
            cartoonInfo?.sortByKey
                ?.takeIf(String::isNotEmpty)
                ?: PlayLineWrapper.SORT_DEFAULT_KEY,
            cartoonInfo?.reversal ?: false,
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
        val isCache: Boolean = false,
        val starDialogState: StarDialogState? = null,
    )

    data class StarDialogState(
        val cartoon: CartoonInfo,
        val tagList: List<CartoonTag>,
    )


    init {
        viewModelScope.launch {
            cartoonRepository.cartoonInfo(cartoonSummary).collectLatest { result ->
                _stateFlow.update { current ->
                    when (result) {
                        is DataResult.Loading -> current.copy(
                            isLoading = current.cartoonInfo == null,
                            isError = false,
                            errorMsg = "",
                            throwable = null,
                        )
                        is DataResult.Ok -> {
                            val isComplete = result.data.isDetailed && result.data.isPlayLineLoad
                            current.copy(
                                isLoading = !isComplete,
                                isError = false,
                                errorMsg = "",
                                throwable = null,
                                cartoonInfo = result.data.takeIf { isComplete }
                                    ?: current.cartoonInfo,
                                isCache = result.isCache,
                            )
                        }
                        is DataResult.Error -> current.copy(
                            isLoading = false,
                            isError = current.cartoonInfo == null,
                            errorMsg = result.errorMsg,
                            throwable = result.throwable,
                        )
                    }
                }
            }
        }
        requestLoad(forceRefresh = false)
    }

    fun load() {
        requestLoad(forceRefresh = true)
    }

    private fun requestLoad(forceRefresh: Boolean) {
        job?.cancel()
        job = viewModelScope.launch {
            "detail-load action=start requestedSource=${cartoonSummary.source} requestedId=${cartoonSummary.id} vm=${System.identityHashCode(this@DetailedViewModel)}".logi("PlaybackTrace")
            cartoonRepository.awaitCartoonInfoWithPlayLines(
                cartoonSummary,
                forceRefresh = forceRefresh,
            )
        }
    }

    fun setCartoonStar(star: Boolean, cartoon: CartoonInfo) {
        if (!cartoon.match(cartoonSummary)) {
            return
        }
        viewModelScope.launch {
            val current = cartoonRepository.cachedCartoonInfo(cartoonSummary) ?: cartoon
            if (star) {
                val tags = cartoonStarController.cartoonTagFlow.first().tagList
                if (tags.any { !it.isInner && !it.isDefault }) {
                    _stateFlow.update {
                        it.copy(
                            starDialogState = StarDialogState(
                                current,
                                tags.filter { tag -> !tag.isInner }.sortedBy { tag -> tag.order },
                            ),
                        )
                    }
                } else {
                    cartoonRepository.updateCartoonInfo(cartoonSummary, fallback = current) {
                        it.copy(
                            starTime = System.currentTimeMillis(),
                            tags = "",
                            isUpdate = false,
                        )
                    }
                }
            } else {
                cartoonRepository.updateCartoonInfo(cartoonSummary, fallback = current) {
                    it.copy(
                        starTime = 0,
                        tags = "",
                        isUpdate = false,
                    )
                }
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
            cartoonRepository.updateCartoonInfo(cartoonSummary, fallback = cartoon) {
                it.copy(
                    starTime = System.currentTimeMillis(),
                    tags = tag.joinToString(", ") { value -> value.label },
                    isUpdate = false,
                )
            }
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
            cartoonRepository.updateCartoonInfo(cartoonSummary, fallback = cartoon) {
                it.copy(
                    sortByKey = sortByKey,
                    reversal = isReverse,
                )
            }
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
