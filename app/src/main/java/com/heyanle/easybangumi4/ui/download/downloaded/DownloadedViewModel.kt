package com.heyanle.easybangumi4.ui.download.downloaded

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.download.LocalCartoonController
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2023/11/2.
 */
class DownloadedViewModel : ViewModel() {

    private val localCartoonController: LocalCartoonController by Injekt.injectLazy()
    private val _localCartoonFlow = MutableStateFlow<List<LocalCartoon>>(emptyList())
    val localCartoonFlow = _localCartoonFlow.asStateFlow()

    val selection = mutableStateMapOf<LocalCartoon, Boolean>()

    private val _keyword = MutableStateFlow<String>("")
    val keyword = _keyword.asStateFlow()

    val focusRequester = FocusRequester()

    init {
        viewModelScope.launch {
            combine(
                localCartoonController.localCartoon.filterIsInstance<List<LocalCartoon>>().stateIn(viewModelScope,),
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

    val removeDownloadItem = mutableStateOf<Collection<LocalCartoon>?>(null)

    fun remove(selection: Collection<LocalCartoon>) {
        selection.forEach {
            localCartoonController.remove(it)
        }
    }

    fun onSelectExit() {
        selection.clear()
    }

    fun onSelectAll() {
        localCartoonFlow.value.forEach {
            selection[it] = true
        }
    }

    fun onSelectInvert() {
        val current = selection.toMap()
        localCartoonFlow.value.forEach {
            if (current.containsKey(it)) {
                selection.remove(it)
            } else {
                selection[it] = true
            }
        }
    }

    fun search(keyword: String) {
        _keyword.update {
            keyword
        }
    }

}