package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.cartoon.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.entity.isChild
import com.heyanle.easybangumi4.cartoon.CartoonRepository
import com.heyanle.easybangumi4.getter.CartoonInfoGetter
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
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

    private val cartoonInfoGetter: CartoonInfoGetter by Injekt.injectLazy()

    private val cartoonStarDao: CartoonStarDao by Injekt.injectLazy()


    fun checkUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            val star = cartoonStarDao.getByCartoonSummary(cartoonSummary.id, cartoonSummary.source, cartoonSummary.url)
            if(star?.isUpdate == true){
                load()
            }
        }
    }
    fun load() {
        viewModelScope.launch {
            detailedState = DetailedState.Loading
            cartoonInfoGetter.awaitCartoonInfoWithPlayLines(cartoonSummary.id, cartoonSummary.source, cartoonSummary.url)
                .onOK {
                    detailedState = DetailedState.Info(it.first, it.second, it.second !is DetailedComponent.NonPlayLine)
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
                withContext(Dispatchers.IO) {
                    cartoonStarDao.modify(CartoonStar.fromCartoonInfo(cartoon, playLines).apply {
                        reversal = isReverse
                    })
                }
                // AnimStarViewModel.refresh()
                if (cartoonSummary.isChild(cartoon)) {
                    this@DetailedViewModel.isStar = true
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

    fun setCartoonReverse(isReverse: Boolean, cartoon: CartoonInfo){
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