package com.heyanle.easybangumi4.ui.download

import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.easybangumi4.getter.LocalCartoonGetter
import org.koin.mp.KoinPlatform.getKoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/10/2 11:24.
 * https://github.com/heyanLE
 */
class LocalCartoonViewModel : ViewModel() {

    private val localCartoonGetter: LocalCartoonGetter by getKoin().inject()

    private val _localCartoonFlow = MutableStateFlow<List<LocalCartoon>>(emptyList())
    val localCartoonFlow = _localCartoonFlow.asStateFlow()
    private val _keyword = MutableStateFlow<String>("")
    val keyword = _keyword.asStateFlow()

    val focusRequester = FocusRequester()

    init {
        viewModelScope.launch {
            combine(
               localCartoonGetter.flowLocalCartoon().stateIn(
                    viewModelScope,
                ),
                _keyword
            ) { list, keyword ->
                _localCartoonFlow.update {
                    if (keyword.isEmpty())
                        list
                    else
                        list.filter {
                            it.match(keyword)
                        }
                }
            }.collect()
        }
    }

    fun search(keyword: String) {
        _keyword.update {
            keyword
        }
    }


}