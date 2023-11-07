package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.isChild
import com.heyanle.easybangumi4.cartoon.tags.CartoonTagsController
import com.heyanle.easybangumi4.cartoon.tags.isInner
import com.heyanle.easybangumi4.getter.CartoonInfoGetter
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/7 15:04.
 * https://github.com/heyanLE
 */
class DetailedViewModel(
    private val cartoonSummary: CartoonSummary,
) : ViewModel() {

    sealed class DetailedState {
        data object None : DetailedState()

        data object Loading : DetailedState()

        class Info(
            val detail: CartoonInfo,
            val playLine: List<PlayLine>,
            val isShowPlayLine: Boolean = true,
        ) : DetailedState()

        class Error(
            val errorMsg: String,
            val throwable: Throwable?
        ) : DetailedState()
    }

    var detailedState by mutableStateOf<DetailedState>(DetailedState.None)
    var isStar by mutableStateOf(false)
    var isReverse by mutableStateOf(false)

    data class StarDialogState(
        val cartoon: CartoonInfo,
        val playLines: List<PlayLine>,
        val tagList: List<CartoonTag>,
    )

    var starDialogState by mutableStateOf<StarDialogState?>(null)

    private val cartoonInfoGetter: CartoonInfoGetter by Injekt.injectLazy()

    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()

    private val cartoonTagsController: CartoonTagsController by Injekt.injectLazy()


    private val tagList = MutableStateFlow<List<CartoonTag>>(emptyList())

    init {
        viewModelScope.launch {
            cartoonTagsController.tagsList.distinctUntilChanged()
                .map {
                    it.filter { !it.isInner() }
                }
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                .collectLatest { l ->
                    tagList.update {
                        l
                    }
                }
        }
    }




    fun checkUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            val star = cartoonStarDao.getByCartoonSummary(
                cartoonSummary.id,
                cartoonSummary.source,
                cartoonSummary.url
            )
            if (star?.isUpdate == true) {
                load()
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            detailedState = DetailedState.Loading
            cartoonInfoGetter.awaitCartoonInfoWithPlayLines(
                cartoonSummary.id,
                cartoonSummary.source,
                cartoonSummary.url
            )
                .onOK {
                    detailedState = DetailedState.Info(
                        it.first,
                        it.second,
                        it.second !is DetailedComponent.NonPlayLine
                    )
                    val starInfo = withContext(Dispatchers.IO) {
                        val cartoonStar = cartoonStarDao.getByCartoonSummary(
                            it.first.id,
                            it.first.source,
                            it.first.url
                        )

                        cartoonStar?.let { star ->
                            val nStar = CartoonStar.fromCartoonInfo(it.first, it.second)
                            cartoonStarDao.update(
                                nStar.copy(
                                    watchProcess = star.watchProcess,
                                    reversal = star.reversal,
                                    createTime = star.createTime,
                                    tags = star.tags,
                                    isUpdate = false
                                )
                            )
                        }
                        cartoonStar
                    }
                    val isStar = starInfo != null
                    val isRev = starInfo?.reversal ?: false
                    this@DetailedViewModel.isStar = isStar
                    isRev.loge("DetailedViewModel")
                    this@DetailedViewModel.isReverse = isRev
                }.onError {
                    detailedState = DetailedState.Error(
                        it.errorMsg,
                        it.throwable
                    )
                }
        }
    }

    fun setCartoonStar(isStar: Boolean, cartoon: CartoonInfo, playLines: List<PlayLine>) {
        viewModelScope.launch {
            if (isStar) {
                val tl = tagList.value
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
                    this@DetailedViewModel.isStar = false
                }
            }
        }
    }

    fun starCartoon(
        cartoon: CartoonInfo,
        playLines: List<PlayLine>,
        tag: List<CartoonTag>
    ) {
        viewModelScope.launch {
            innerStarCartoon(cartoon, playLines, tag)
        }
    }

    private suspend fun innerStarCartoon(
        cartoon: CartoonInfo,
        playLines: List<PlayLine>,
        tag: List<CartoonTag>
    ) {
        withContext(Dispatchers.IO) {
            cartoonStarDao.modify(
                CartoonStar.fromCartoonInfo(
                    cartoon,
                    playLines,
                    tag.joinToString(", ") { it.id.toString() }).apply {
                    reversal = isReverse
                })
        }
        // AnimStarViewModel.refresh()
        if (cartoonSummary.isChild(cartoon)) {
            this@DetailedViewModel.isStar = true
        }
    }

    fun setCartoonReverse(isReverse: Boolean, cartoon: CartoonInfo) {
        this.isReverse = isReverse
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
                            reversal = isReverse
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