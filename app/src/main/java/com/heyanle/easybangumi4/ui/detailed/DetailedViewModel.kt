package com.heyanle.easybangumi4.ui.detailed

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
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/4 15:30.
 * https://github.com/heyanLE
 */
class DetailedViewModel (
    private val cartoonSummary: CartoonSummary,
    private val detailedComponent: DetailedComponent,
): ViewModel() {

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

    fun load(){
        viewModelScope.launch {
            detailedState = DetailedState.Loading
            detailedComponent.getAll(cartoonSummary)
                .complete {
                    detailedState = DetailedState.Info(it.data.first, it.data.second)
                }.error {
                    detailedState = DetailedState.Error(
                        if (it.isParserError) stringRes(
                            com.heyanle.easy_i18n.R.string.source_error
                        ) else stringRes(com.heyanle.easy_i18n.R.string.loading_error),
                        it.throwable
                    )
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