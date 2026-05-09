package org.easybangumi.next.shared.story

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.download.DownloadManager
import org.easybangumi.next.shared.download.model.DownloadInfo
import org.easybangumi.next.shared.download.model.DownloadReq
import org.easybangumi.next.shared.local.LocalCartoonController
import org.easybangumi.next.shared.local.LocalCartoonItem

/**
 * Story 控制器
 * 协调下载系统和本地源
 */
class StoryController(
    private val downloadManager: DownloadManager,
    private val localController: LocalCartoonController,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val downloadInfos: StateFlow<List<DownloadInfo>> = downloadManager.downloadInfos
    val localItems: StateFlow<List<LocalCartoonItem>> = localController.items

    init {
        // 下载完成后自动刷新本地源
        scope.launch {
            downloadManager.downloadInfos.collect { infos ->
                // 检查是否有新完成的下载
                // 这里简单处理，实际可以通过状态变化来触发
            }
        }
    }

    /**
     * 创建新的下载
     */
    suspend fun newDownload(
        cartoonInfo: CartoonInfo,
        playLine: PlayerLine,
        episodes: List<Episode>,
        targetLocal: LocalCartoonItem? = null,
        playInfoType: String,
    ): List<DownloadReq> {
        return downloadManager.newDownload(
            cartoonInfo = cartoonInfo,
            playLine = playLine,
            episodes = episodes,
            targetLocal = targetLocal,
            playInfoType = playInfoType,
        )
    }

    /**
     * 暂停下载
     */
    fun pauseDownload(uuid: String) {
        downloadManager.pause(uuid)
    }

    /**
     * 恢复下载
     */
    fun resumeDownload(uuid: String) {
        downloadManager.resume(uuid)
    }

    /**
     * 取消下载
     */
    fun cancelDownload(uuid: String) {
        downloadManager.cancel(uuid)
    }

    /**
     * 删除已完成的下载
     */
    fun removeCompleted(uuid: String) {
        downloadManager.removeCompleted(uuid)
    }

    /**
     * 删除本地番剧（级联删除下载请求）
     */
    fun deleteLocalItem(itemId: String) {
        downloadManager.deleteLocalItem(itemId)
    }

    /**
     * 删除本地番剧单集
     */
    fun deleteEpisode(item: LocalCartoonItem, episode: org.easybangumi.next.shared.local.LocalEpisode) {
        localController.deleteEpisode(item, episode)
    }

    /**
     * 刷新本地番剧
     */
    fun refreshLocal() {
        localController.refresh()
    }
}
