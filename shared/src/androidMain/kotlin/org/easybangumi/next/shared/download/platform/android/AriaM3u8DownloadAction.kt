package org.easybangumi.next.shared.download.platform.android

import org.easybangumi.next.shared.download.action.DownloadChain
import org.easybangumi.next.shared.download.action.DownloadAction
import org.easybangumi.next.shared.download.model.DownloadRuntime

/**
 * Android Aria M3U8 下载
 * 注意：此实现需要 Aria 库依赖，仅在 Android 平台可用
 */
class AriaM3u8DownloadAction : DownloadAction {

    override val name = DownloadChain.ACTION_ARIA_M3U8

    override fun isAsync() = true

    override suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean {
        // TODO: 检查 Aria 是否有未完成的任务
        return false
    }

    override suspend fun execute(runtime: DownloadRuntime) {
        val playerInfo = runtime.playerInfo
            ?: throw IllegalStateException("playerInfo is null")

        runtime.reportStatus("M3U8 下载中...")

        // TODO: 使用 Aria 库下载 M3U8
        // 1. 配置 M3U8VodOption
        // 2. 创建下载任务
        // 3. 监听进度回调
        // 4. 下载完成后设置 cacheFilePath

        throw NotImplementedError("AriaM3u8DownloadAction 需要 Aria 库依赖")
    }

    override suspend fun pause(runtime: DownloadRuntime): Boolean {
        // TODO: 暂停 Aria 任务
        runtime.markPaused()
        return true
    }

    override suspend fun resume(runtime: DownloadRuntime): Boolean {
        // TODO: 恢复 Aria 任务
        runtime.markResumed()
        return true
    }

    override suspend fun cancel(runtime: DownloadRuntime) {
        // TODO: 取消 Aria 任务
        runtime.markCanceled()
    }

    override suspend fun onTaskComplete(runtime: DownloadRuntime) {
        // TODO: 清理 Aria 任务
    }
}
