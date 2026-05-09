package org.easybangumi.next.shared.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.download.model.DownloadInfo
import org.easybangumi.next.shared.download.model.DownloadState
import org.easybangumi.next.shared.local.LocalCartoonItem

/**
 * Story 页面 ViewModel
 */
class StoryViewModel(
    private val storyController: StoryController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()

    init {
        // 收集下载信息
        viewModelScope.launch {
            storyController.downloadInfos.collect { infos ->
                _uiState.value = _uiState.value.copy(
                    downloadingItems = infos.filter { it.isActive },
                    completedItems = infos.filter { it.isComplete },
                    errorItems = infos.filter { it.isError },
                )
            }
        }

        // 收集本地番剧
        viewModelScope.launch {
            storyController.localItems.collect { items ->
                _uiState.value = _uiState.value.copy(
                    localItems = items,
                )
            }
        }
    }

    /**
     * 暂停下载
     */
    fun pauseDownload(uuid: String) {
        storyController.pauseDownload(uuid)
    }

    /**
     * 恢复下载
     */
    fun resumeDownload(uuid: String) {
        storyController.resumeDownload(uuid)
    }

    /**
     * 取消下载
     */
    fun cancelDownload(uuid: String) {
        storyController.cancelDownload(uuid)
    }

    /**
     * 删除已完成的下载
     */
    fun removeCompleted(uuid: String) {
        storyController.removeCompleted(uuid)
    }

    /**
     * 删除本地番剧
     */
    fun deleteLocalItem(itemId: String) {
        storyController.deleteLocalItem(itemId)
    }

    /**
     * 刷新本地番剧
     */
    fun refreshLocal() {
        storyController.refreshLocal()
    }
}

/**
 * Story 页面 UI 状态
 */
data class StoryUiState(
    val downloadingItems: List<DownloadInfo> = emptyList(),
    val completedItems: List<DownloadInfo> = emptyList(),
    val errorItems: List<DownloadInfo> = emptyList(),
    val localItems: List<LocalCartoonItem> = emptyList(),
    val selectedTab: StoryTab = StoryTab.DOWNLOADING,
)

/**
 * Story Tab 枚举
 */
enum class StoryTab {
    DOWNLOADING,
    COMPLETED,
    LOCAL,
}
