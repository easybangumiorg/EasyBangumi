package org.easybangumi.next.shared.download.platform.android

import org.easybangumi.next.shared.download.action.DownloadChain
import org.easybangumi.next.shared.download.action.DownloadAction
import org.easybangumi.next.shared.download.model.DownloadRuntime

/**
 * Android Media3 Transformer 转码
 * 注意：此实现需要 Media3 依赖，仅在 Android 平台可用
 */
class TransformerAction : DownloadAction {

    override val name = DownloadChain.ACTION_TRANSCODE

    override fun isAsync() = true

    override suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean {
        return false
    }

    override suspend fun execute(runtime: DownloadRuntime) {
        val cacheFilePath = runtime.cacheFilePath
            ?: throw IllegalStateException("cacheFilePath is null")

        runtime.reportStatus("转码中...")

        // TODO: 使用 Media3 Transformer 转码
        // 1. 创建 Transformer 实例
        // 2. 配置编码参数（H.264/H.265）
        // 3. 执行转码
        // 4. 监听进度回调
        // 5. 转码完成后更新 cacheFilePath

        throw NotImplementedError("TransformerAction 需要 Media3 依赖")
    }

    override suspend fun pause(runtime: DownloadRuntime): Boolean {
        // TODO: 暂停转码
        runtime.markPaused()
        return true
    }

    override suspend fun resume(runtime: DownloadRuntime): Boolean {
        // TODO: 恢复转码
        runtime.markResumed()
        return true
    }

    override suspend fun cancel(runtime: DownloadRuntime) {
        // TODO: 取消转码
        runtime.markCanceled()
    }

    override suspend fun onTaskComplete(runtime: DownloadRuntime) {
        // TODO: 清理转码临时文件
    }
}
