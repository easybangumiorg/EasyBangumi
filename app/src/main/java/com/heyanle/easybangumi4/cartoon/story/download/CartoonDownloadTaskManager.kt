package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineDescriptor
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineRegistry
import com.heyanle.easybangumi4.cartoon.story.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadDispatcher

/**
 * 下载任务的唯一命令入口。持久任务是事实来源，runtime 只是一次执行尝试。
 */
class CartoonDownloadTaskManager(
    private val requestController: CartoonDownloadReqController,
    private val dispatcher: CartoonDownloadDispatcher,
    private val engineRegistry: QuickDownloadEngineRegistry,
) {

    val quickDownloadEngines: List<QuickDownloadEngineDescriptor>
        get() = engineRegistry.descriptors

    fun enqueue(requests: Collection<CartoonDownloadReq>) {
        if (requests.isEmpty()) return
        requestController.newDownloadItem(requests)
        dispatcher.newRequest(requests)
    }

    fun remove(requests: Collection<CartoonDownloadReq>) {
        if (requests.isEmpty()) return
        // 先停止 runtime，避免持久任务删除后旧 action 继续发布结果。
        dispatcher.remove(requests) {
            requestController.removeDownloadItem(requests.map { it.uuid })
        }
    }

    fun removeByLocalItemIds(itemIds: Collection<String>) {
        remove(requestController.findDownloadItemsByLocalItemIds(itemIds))
    }

    fun retry(taskId: String) {
        val request = requestController.findDownloadItem(taskId) ?: return
        dispatcher.tryResume(listOf(request))
    }

    suspend fun toggle(taskId: String): Boolean = dispatcher.toggle(taskId)

    fun switchQuickEngine(taskId: String, engineId: String): Boolean {
        val request = requestController.findDownloadItem(taskId) ?: return false
        if (!DownloadTaskSwitchPolicy.canSwitchEngine(
                isQuickMode = request.quickMode,
                currentEngineId = request.quickDownloadEngineId,
                targetEngineId = engineId,
                targetEngineAvailable = engineRegistry.find(engineId) != null,
                runtimeReplaceable = dispatcher.canReplace(taskId),
            )
        ) {
            return false
        }
        val replacement = request.copy(quickDownloadEngineId = engineId)
        dispatcher.restart(
            req = replacement,
            discardCheckpoint = true,
            beforeStart = { requestController.replaceDownloadItem(replacement) },
        )
        return true
    }

    fun retryAsFullDownload(taskId: String): Boolean {
        val request = requestController.findDownloadItem(taskId) ?: return false
        if (!dispatcher.canReplace(taskId)) return false
        if (!request.quickMode) {
            retry(taskId)
            return true
        }
        val replacement = request.copy(quickMode = false)
        dispatcher.restart(
            req = replacement,
            discardCheckpoint = true,
            beforeStart = { requestController.replaceDownloadItem(replacement) },
        )
        return true
    }
}

internal object DownloadTaskSwitchPolicy {
    fun canSwitchEngine(
        isQuickMode: Boolean,
        currentEngineId: String,
        targetEngineId: String,
        targetEngineAvailable: Boolean,
        runtimeReplaceable: Boolean,
    ): Boolean {
        return isQuickMode &&
            currentEngineId != targetEngineId &&
            targetEngineAvailable &&
            runtimeReplaceable
    }
}
