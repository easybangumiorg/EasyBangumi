package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.collection.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/7 15:04.
 * https://github.com/heyanLE
 */
class DetailedViewModel(
    val cartoonSummary: CartoonSummary,
    private val detailedComponent: DetailedComponent,
) : ViewModel() {

    sealed class DetailedState {
        object None : DetailedState()

        object Loading : DetailedState()

        class Info(
            val detail: Cartoon,
            val playLine: List<PlayLine>,

            ) : DetailedState()

        class Error(
            val errorMsg: String,
            val throwable: Throwable?
        ) : DetailedState()
    }

    var detailedState by mutableStateOf<DetailedState>(DetailedState.None)
    var isStar by mutableStateOf(false)

    fun load(){
        viewModelScope.launch {
            detailedState = DetailedState.Loading
            detailedComponent.getAll(cartoonSummary)
                .complete {

                    detailedState = DetailedState.Info(it.data.first, it.data.second)
                    val isStar = withContext(Dispatchers.IO) {
                        DB.cartoonStar.getByCartoonSummary(
                            it.data.first.id,
                            it.data.first.source,
                            it.data.first.url
                        ) != null
                    }
                    this@DetailedViewModel.isStar = isStar
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

    fun setCartoonStar(isStar: Boolean, cartoon: Cartoon, playLines: List<PlayLine>){
        viewModelScope.launch {
            if (isStar) {
                withContext(Dispatchers.IO) {
                    DB.cartoonStar.modify(CartoonStar.fromCartoon(cartoon, playLines))
                }
                // AnimStarViewModel.refresh()
                if(cartoonSummary.isChild(cartoon)){
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
                if(cartoonSummary.isChild(cartoon)){
                    this@DetailedViewModel.isStar = false
                }
            }
        }
    }



    /**
     * lru 复用
     */
    companion object {
        private val viewModelOwnerStore = object: LruCache<CartoonSummary, ViewModelStore>(3){
            override fun entryRemoved(
                evicted: Boolean,
                key: CartoonSummary,
                oldValue: ViewModelStore,
                newValue: ViewModelStore?
            ) {
                super.entryRemoved(evicted, key, oldValue, newValue)
                oldValue.clear()
            }
        }

        fun getViewModelStoreOwner(summer: CartoonSummary) = object: ViewModelStoreOwner {

            override val viewModelStore: ViewModelStore
                get() {
                    var viewModelStore = viewModelOwnerStore.get(summer)
                    if (viewModelStore == null) {
                        viewModelStore = ViewModelStore()
                        viewModelOwnerStore.put(summer, viewModelStore)
                    }
                    return viewModelStore
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