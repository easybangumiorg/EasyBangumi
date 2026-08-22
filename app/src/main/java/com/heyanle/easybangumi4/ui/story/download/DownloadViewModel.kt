package com.heyanle.easybangumi4.ui.story.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineDescriptor
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DownloadViewModel : ViewModel() {

    private val cartoonStoryController: CartoonStoryController by Inject.injectLazy()

    data class State(
        val loading: Boolean = true,
        val errorMessage: String? = null,
        val downloadInfo: List<CartoonDownloadInfo> = emptyList(),
        val selectionIds: Set<String> = emptySet(),
        val quickDownloadEngines: List<QuickDownloadEngineDescriptor> = emptyList(),
        val dialog: Dialog? = null,
    )

    sealed class Dialog {
        data class DeleteSelection(val taskIds: Set<String>) : Dialog()
        data class ResumeTask(val taskId: String) : Dialog()
    }

    private val _state = MutableStateFlow(
        State(quickDownloadEngines = cartoonStoryController.quickDownloadEngines)
    )
    val state = _state.asStateFlow()

    private var lastDownloadInfo: CartoonDownloadInfo? = null

    init {
        viewModelScope.launch {
            cartoonStoryController.downloadInfoList.collectLatest { result ->
                when (result) {
                    is DataResult.Loading -> _state.update {
                        it.copy(loading = true, errorMessage = null)
                    }

                    is DataResult.Error -> _state.update {
                        it.copy(
                            loading = false,
                            errorMessage = result.errorMsg.ifBlank { "读取下载任务失败" },
                        )
                    }

                    is DataResult.Ok -> _state.update {
                        val latestIds = result.data.mapTo(mutableSetOf()) { info -> info.req.uuid }
                        it.copy(
                            loading = false,
                            errorMessage = null,
                            downloadInfo = result.data,
                            selectionIds = retainValidTaskSelection(
                                selectionIds = it.selectionIds,
                                latestTaskIds = latestIds,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun clickDownloadInfo(info: CartoonDownloadInfo) {
        val runtime = info.runtime
        if (runtime == null || runtime.isCanceled() || runtime.isError() || runtime.isPaused()) {
            if (info.req.quickMode) {
                _state.update { it.copy(dialog = Dialog.ResumeTask(info.req.uuid)) }
            } else {
                cartoonStoryController.retryDownloadReq(info.req.uuid)
            }
            return
        }

        viewModelScope.launch {
            if (!cartoonStoryController.toggleDownloadReq(info.req.uuid)) {
                "当前步骤不支持暂停或继续".moeSnackBar()
            }
        }
    }

    fun retry(taskId: String) {
        cartoonStoryController.retryDownloadReq(taskId)
    }

    fun resume(taskId: String) {
        viewModelScope.launch {
            if (!cartoonStoryController.toggleDownloadReq(taskId)) {
                "当前任务无法继续".moeSnackBar()
            }
        }
    }

    fun switchEngine(taskId: String, engineId: String) {
        cartoonStoryController.switchQuickDownloadEngine(taskId, engineId)
    }

    fun retryAsFull(taskId: String) {
        cartoonStoryController.retryAsFullDownload(taskId)
    }

    fun selectDownloadInfo(info: CartoonDownloadInfo) {
        val taskId = info.req.uuid
        _state.update {
            val selection = it.selectionIds.toMutableSet().apply {
                if (!add(taskId)) remove(taskId)
            }
            lastDownloadInfo = if (selection.isEmpty()) null else info
            it.copy(selectionIds = selection)
        }
    }

    fun onSelectionLongPress(info: CartoonDownloadInfo) {
        if (lastDownloadInfo == null || lastDownloadInfo == info) {
            selectDownloadInfo(info)
            return
        }
        _state.update {
            val selection = it.selectionIds.toMutableSet()
            val list = it.downloadInfo
            var anchor = list.indexOf(lastDownloadInfo)
            val pressed = list.indexOf(info)
            if (pressed > anchor) anchor += 1 else if (anchor > pressed) anchor -= 1
            val start = anchor.coerceAtMost(pressed)
            val end = anchor.coerceAtLeast(pressed)
            for (index in start..end) {
                val taskId = list.getOrNull(index)?.req?.uuid ?: continue
                if (!selection.add(taskId)) selection.remove(taskId)
            }
            it.copy(selectionIds = selection)
        }
        lastDownloadInfo = info
    }

    fun selectAll() {
        _state.update {
            it.copy(selectionIds = it.downloadInfo.mapTo(mutableSetOf()) { info -> info.req.uuid })
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectionIds = emptySet()) }
        lastDownloadInfo = null
    }

    fun showDeleteDialog() {
        _state.update {
            it.copy(
                dialog = Dialog.DeleteSelection(it.selectionIds),
                selectionIds = emptySet(),
            )
        }
        lastDownloadInfo = null
    }

    fun dismissDialog() {
        _state.update { it.copy(dialog = null) }
    }

    fun deleteDownload(taskIds: Set<String>) {
        val requests = _state.value.downloadInfo
            .filter { it.req.uuid in taskIds }
            .map { it.req }
        cartoonStoryController.removeDownloadReq(requests)
    }
}

internal fun retainValidTaskSelection(
    selectionIds: Set<String>,
    latestTaskIds: Set<String>,
): Set<String> = selectionIds.intersect(latestTaskIds)
