package com.heyanle.easybangumi4.compose.cartoon_play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/7 15:04.
 * https://github.com/heyanLE
 */
class DetailedViewModel(
    private val cartoonSummary: CartoonSummary,
    private val detailedComponent: DetailedComponent,
) : ViewModel() {

    sealed class DetailedState {
        object None : DetailedState()

        object Loading : DetailedState()

        class Info(
            val detail: Cartoon,
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

    fun checkUpdate(){
        viewModelScope.launch(Dispatchers.IO) {
            val star = DB.cartoonStar.getByCartoonSummary(cartoonSummary.id, cartoonSummary.source, cartoonSummary.url)
            if(star?.isUpdate == true){
                load()
            }
        }
    }
    fun load() {
        viewModelScope.launch {
            detailedState = DetailedState.Loading
            detailedComponent.getAll(cartoonSummary)
                .complete {
//                    it.data.second.loge("DetailedViewModel")
//                    it.data.second.size.loge("DetailedViewModel")
//                    it.data.second.first().episode.size.loge("DetailedViewModel")
                    detailedState = DetailedState.Info(it.data.first, it.data.second, it.data.second !is DetailedComponent.NonPlayLine)
                    val starInfo = withContext(Dispatchers.IO) {
                        val cartoonStar = DB.cartoonStar.getByCartoonSummary(
                            it.data.first.id,
                            it.data.first.source,
                            it.data.first.url
                        )

                        cartoonStar?.let { star ->
                            val nStar = CartoonStar.fromCartoon(it.data.first, it.data.second)
                            DB.cartoonStar.update(
                                nStar.copy(
                                    watchProcess = star.watchProcess,
                                    reversal = star.reversal,
                                    createTime = star.createTime,
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
                }.error {
                    detailedState = DetailedState.Error(
                        if (it.isParserError) stringRes(
                            R.string.source_error
                        ) else stringRes(R.string.loading_error),
                        it.throwable
                    )
                }
        }
    }

    fun setCartoonStar(isStar: Boolean, cartoon: Cartoon, playLines: List<PlayLine>) {
        viewModelScope.launch {
            if (isStar) {
                withContext(Dispatchers.IO) {
                    DB.cartoonStar.modify(CartoonStar.fromCartoon(cartoon, playLines).apply {
                        reversal = isReverse
                    })
                }
                // AnimStarViewModel.refresh()
                if (cartoonSummary.isChild(cartoon)) {
                    this@DetailedViewModel.isStar = true
                }
            } else {
                withContext(Dispatchers.IO) {
                    DB.cartoonStar
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

    fun setCartoonReverse(isReverse: Boolean, cartoon: Cartoon){
        this.isReverse = isReverse
        if (isStar) {
            viewModelScope.launch(Dispatchers.IO) {
                val cartoonStar = DB.cartoonStar.getByCartoonSummary(
                    cartoon.id,
                    cartoon.source,
                    cartoon.url
                )

                cartoonStar?.let { star ->
                    DB.cartoonStar.update(
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
    private val detailedComponent: DetailedComponent,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailedViewModel::class.java))
            return DetailedViewModel(cartoonSummary, detailedComponent) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}