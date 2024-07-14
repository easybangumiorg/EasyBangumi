package com.heyanle.easybangumi4.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.CartoonLocalDownloadController
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
class DownloadViewModel: ViewModel() {


    val downloadDispatcher: CartoonDownloadDispatcher by Inject.injectLazy()
    val localDownloadController: CartoonLocalDownloadController by Inject.injectLazy()

    data class State(
        val loading: Boolean = true,
        val downloadInfo: List<CartoonDownloadInfo> = listOf(),
        val selection : Set<CartoonDownloadInfo> = emptySet()
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var lastDownloadInfo: CartoonDownloadInfo? = null

    init {
        viewModelScope.launch {
            localDownloadController.downloadInfo.collectLatest {
                _state.value = _state.value.copy(
                    loading = false,
                    downloadInfo = it
                )
            }

        }
    }

    fun clickDownloadInfo(info: CartoonDownloadInfo){

    }


    fun selectDownloadInfo(info: CartoonDownloadInfo){
        _state.update {
            it.copy(
                selection = _state.value.selection.toMutableSet().apply {
                    if(contains(info)){
                        remove(info)
                    }else{
                        add(info)
                    }
                }
            ).apply {
                lastDownloadInfo = if (selection.isEmpty()) {
                    null
                } else {
                    info

                }
            }
        }
    }

    fun onSelectionLongPress(info: CartoonDownloadInfo) {
        if (lastDownloadInfo == null || lastDownloadInfo == info) {
            selectDownloadInfo(info)
            return
        }
        _state.update {
            val selection = it.selection.toMutableSet()
            val lastList = it.downloadInfo
            var a = lastList.indexOf(lastDownloadInfo)
            val b = lastList.indexOf(info)
            if (b > a) {
                a += 1
            } else if (a > b) {
                a -= 1
            }
            val start = a.coerceAtMost(b)
            val end = a.coerceAtLeast(b)
            for (i in start..end) {
                if (i >= 0 && i < lastList.size) {
                    val star = lastList[i]
                    if (selection.contains(star)) {
                        selection.remove(star)
                    } else {
                        selection.add(star)
                    }
                }
            }
            it.copy(
                selection = selection
            )
        }
        lastDownloadInfo = info
    }

    fun selectAll(){
        _state.value = _state.value.copy(
            selection = _state.value.downloadInfo.toSet()
        )
    }

    fun clearSelection(){
        _state.value = _state.value.copy(
            selection = emptySet()
        )
    }



    fun deleteSelection(){

    }


}