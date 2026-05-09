package org.easybangumi.next.shared.download

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.download.action.DownloadChain
import org.easybangumi.next.shared.download.model.DownloadInfo
import org.easybangumi.next.shared.download.model.DownloadReq
import org.easybangumi.next.shared.local.LocalCartoonController
import org.easybangumi.next.shared.local.LocalCartoonItem
import org.easybangumi.next.shared.local.LocalItemFactory
import org.easybangumi.next.shared.local.LocalPreference
import org.easybangumi.next.lib.unifile.UniFileFactory

/**
 * 下载管理器
 * 对外统一接口，协调 ReqController + Dispatcher
 */
class DownloadManager(
    private val reqController: DownloadReqController,
    private val dispatcher: DownloadDispatcher,
    private val localController: LocalCartoonController,
    private val localPreference: LocalPreference,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val downloadInfos: StateFlow<List<DownloadInfo>> = dispatcher.downloadInfos

    init {
        // 设置完成回调
        dispatcher.onComplete = { req ->
            reqController.remove(req.uuid)
            localController.refresh()
        }

        // 加载已持久化的请求
        scope.launch {
            reqController.requests.collect { requests ->
                dispatcher.setInitialRequests(requests)
                // 恢复未完成的请求
                for (req in requests) {
                    if (dispatcher.runtimeMap.value[req.uuid] == null) {
                        dispatcher.submit(req)
                    }
                }
            }
        }
    }

    /**
     * 创建新的下载请求
     */
    suspend fun newDownload(
        cartoonInfo: CartoonInfo,
        playLine: PlayerLine,
        episodes: List<Episode>,
        targetLocal: LocalCartoonItem? = null,
        playInfoType: String,
    ): List<DownloadReq> {
        // 确定目标本地番剧
        val localItem = targetLocal ?: findOrCreateLocalItem(cartoonInfo)

        // 获取已存在的剧集编号
        val existingEpisodes = localController.getExistingEpisodes(localItem.itemId)
        val pendingEpisodes = downloadInfos.value
            .filter { it.req.toLocalItemId == localItem.itemId }
            .map { it.req.toEpisode }
            .toSet()

        val allTaken = existingEpisodes + pendingEpisodes

        // 获取下载链
        val stepChain = DownloadChain.getChain(playInfoType)

        // 创建下载请求
        val sortedEpisodes = episodes.sortedBy { it.order }
        val reqs = mutableListOf<DownloadReq>()

        for ((index, episode) in sortedEpisodes.withIndex()) {
            // 找到下一个可用的剧集编号
            var targetEpisode = episode.order
            while (targetEpisode in allTaken) {
                targetEpisode++
            }

            val req = DownloadReq(
                uuid = "req-${Clock.System.now().toEpochMilliseconds()}-$index",
                fromCartoonId = cartoonInfo.fromId,
                fromCartoonSource = cartoonInfo.fromSourceKey,
                fromCartoonName = cartoonInfo.name,
                fromPlayLineId = playLine.id,
                fromPlayLineLabel = playLine.label,
                fromEpisodeId = episode.id,
                fromEpisodeLabel = episode.label,
                fromEpisodeOrder = episode.order,
                toLocalItemId = localItem.itemId,
                toEpisodeTitle = episode.label,
                toEpisode = targetEpisode,
                stepChain = stepChain,
            )

            reqs.add(req)
            reqController.save(req)
            dispatcher.submit(req)
        }

        return reqs
    }

    /**
     * 暂停下载
     */
    fun pause(uuid: String) {
        dispatcher.pause(uuid)
    }

    /**
     * 恢复下载
     */
    fun resume(uuid: String) {
        dispatcher.resume(uuid)
    }

    /**
     * 取消下载
     */
    fun cancel(uuid: String) {
        dispatcher.cancel(uuid)
        scope.launch {
            reqController.remove(uuid)
        }
    }

    /**
     * 删除已完成的下载
     */
    fun removeCompleted(uuid: String) {
        scope.launch {
            reqController.remove(uuid)
        }
    }

    /**
     * 删除本地番剧（级联删除下载请求）
     */
    fun deleteLocalItem(itemId: String) {
        scope.launch {
            // 取消所有相关下载
            val relatedReqs = reqController.getAll().filter { it.toLocalItemId == itemId }
            for (req in relatedReqs) {
                dispatcher.cancel(req.uuid)
            }
            // 删除持久化请求
            reqController.removeByLocalItemId(itemId)
            // 删除本地文件
            val item = localController.findByItemId(itemId)
            if (item != null) {
                localController.deleteItem(item)
            }
        }
    }

    /**
     * 查找或创建本地番剧条目
     */
    private suspend fun findOrCreateLocalItem(cartoonInfo: CartoonInfo): LocalCartoonItem {
        // 先查找是否已存在
        val existing = localController.findByCartoonId(cartoonInfo.fromId)
        if (existing != null) return existing

        // 创建新的本地番剧文件夹
        val rootFolder = localPreference.getLocalFolder()
            ?: throw IllegalStateException("本地目录不可用")

        val itemId = cartoonInfo.fromId
        val folder = LocalItemFactory.createItemFolder(
            rootFolder = rootFolder,
            itemId = itemId,
            title = cartoonInfo.name,
            desc = cartoonInfo.intro,
            coverUrl = cartoonInfo.coverUrl,
            tags = cartoonInfo.tagsIdListString.split(",").filter { it.isNotBlank() },
        ) ?: throw IllegalStateException("无法创建本地番剧文件夹")

        // 刷新本地源
        localController.refresh()

        return localController.findByItemId(itemId)
            ?: throw IllegalStateException("创建后找不到本地番剧")
    }
}
