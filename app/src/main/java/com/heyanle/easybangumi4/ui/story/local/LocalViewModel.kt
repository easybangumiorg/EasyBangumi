package com.heyanle.easybangumi4.ui.story.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
class LocalViewModel: ViewModel() {

    val cartoonStoryController: CartoonStoryController by Inject.injectLazy()
    data class State(
        val loading: Boolean = true,
        val storyList: List<CartoonStoryItem> = listOf(),
        val selection : Set<CartoonStoryItem> = emptySet(),
        val searchKey: String? = null,
        val dialog: Dialog? = null,
    )


    sealed class Dialog {
        data class DeleteSelection(val selection : Set<CartoonStoryItem>): Dialog()
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var lastStoryItem: CartoonStoryItem? = null

    init {
        viewModelScope.launch {
            combine(
                cartoonStoryController.storyItemList,
                state.map { it.searchKey }.distinctUntilChanged(),
            ) {re, keyword ->
                re to keyword
            }.collectLatest { (re, keyword) ->
                when (re) {
                    is DataResult.Loading -> {
                        _state.update {
                            it.copy(
                                loading = true
                            )
                        }
                    }
                    else -> {
                        if (!keyword.isNullOrEmpty()) {
                            _state.update {
                                it.copy(
                                    loading = false,
                                    storyList = re.okOrNull()?.filter {
                                        it.cartoonLocalItem.matches(keyword)
                                    } ?: emptyList()
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    loading = false,
                                    storyList = re.okOrNull() ?: emptyList()
                                )
                            }
                        }
                    }

                }
            }

        }
    }



    fun selectDownloadInfo(info: CartoonStoryItem){
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
                lastStoryItem = if (selection.isEmpty()) {
                    null
                } else {
                    info

                }
            }
        }
    }

    fun onSelectionLongPress(info: CartoonStoryItem) {
        if (lastStoryItem == null || lastStoryItem == info) {
            selectDownloadInfo(info)
            return
        }
        _state.update {
            val selection = it.selection.toMutableSet()
            val lastList = it.storyList
            var a = lastList.indexOf(lastStoryItem)
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
        lastStoryItem = info
    }

    fun selectAll(){
        _state.update {
            it.copy(
                selection = it.storyList.toSet()
            )
        }
    }

    fun clearSelection(){
        _state.value = _state.value.copy(
            selection = emptySet()
        )
    }



    fun showDeleteDialog(){
        _state.update {
            it.copy(
                dialog = Dialog.DeleteSelection(it.selection),
                selection = emptySet()
            )
        }
        lastStoryItem = null
    }

    fun tagSelection(selection: Set<CartoonStoryItem>) {

    }

    fun changeKey(key: String?){
        _state.update {
            it.copy(
                searchKey = key
            )
        }
    }


    fun dismissDialog(){
        _state.update {
            it.copy(
                dialog = null
            )
        }
    }

    fun deleteDownload(selection: Set<CartoonStoryItem>) {
        cartoonStoryController.removeStory(selection)
    }
}