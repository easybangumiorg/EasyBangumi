package com.heyanle.easybangumi4.ui.story.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
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


    val cartoonStoryController: CartoonStoryController by Inject.injectLazy()

    data class State(
        val loading: Boolean = true,
        val downloadInfo: List<CartoonDownloadInfo> = listOf(),
        val selection : Set<CartoonDownloadInfo> = emptySet(),
        val dialog: Dialog? = null,
    )


    sealed class Dialog {
        data class DeleteSelection(val selection : Set<CartoonDownloadInfo>): Dialog()
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var lastDownloadInfo: CartoonDownloadInfo? = null

    init {
        viewModelScope.launch {
            cartoonStoryController.downloadInfoList.collectLatest { re ->
                when (re) {
                    is DataResult.Loading -> {
                        _state.update {
                            it.copy(
                                loading = true
                            )
                        }
                    }
                    else -> {
                        _state.update {
                            it.copy(
                                loading = false,
                                downloadInfo = re.okOrNull() ?: emptyList()
                            )
                        }
                    }

                }
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
        _state.update {
            it.copy(
                selection = it.downloadInfo.toSet()
            )
        }
    }

    fun clearSelection(){
        _state.update {
            it.copy(
                selection = emptySet()
            )
        }
        lastDownloadInfo = null
    }



    fun showDeleteDialog(){
        _state.update {
            it.copy(
                dialog = Dialog.DeleteSelection(it.selection),
                selection = emptySet()
            )
        }
        lastDownloadInfo = null
    }


    fun dismissDialog(){
        _state.update {
            it.copy(
                dialog = null
            )
        }
    }

    fun deleteDownload(selection: Set<CartoonDownloadInfo>) {
        cartoonStoryController.removeDownloadReq(selection.map { it.req })
    }

}