package com.heyanle.easybangumi4.cartoon.story.download.engine

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.action.BaseAction
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime

/**
 * 快速下载流水线的稳定入口。具体引擎由任务快照中的 engineId 决定。
 */
class QuickDownloadAction(
    private val engineRegistry: QuickDownloadEngineRegistry,
) : BaseAction {

    companion object {
        const val NAME = "QuickDownloadAction"
    }

    override suspend fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean {
        return engineRegistry.find(cartoonDownloadReq.quickDownloadEngineId)
            ?.canResume(cartoonDownloadReq)
            ?: false
    }

    override suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        val result = engineRegistry.find(cartoonDownloadRuntime.req.quickDownloadEngineId)
            ?.toggle(cartoonDownloadRuntime.req.uuid)
            ?: QuickDownloadToggleResult.UNSUPPORTED
        cartoonDownloadRuntime.state = when (result) {
            QuickDownloadToggleResult.PAUSED -> CartoonDownloadRuntime.State.PAUSED
            QuickDownloadToggleResult.RESUMED -> CartoonDownloadRuntime.State.DOING
            QuickDownloadToggleResult.UNSUPPORTED -> return false
        }
        return true
    }

    override fun push(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        val engineId = cartoonDownloadRuntime.req.quickDownloadEngineId
        val engine = engineRegistry.find(engineId)
        if (engine == null) {
            cartoonDownloadRuntime.error(
                errorMsg = "下载引擎不可用：$engineId"
            )
            return
        }
        val resolvedPlayerInfo = cartoonDownloadRuntime.playerInfo ?: run {
            cartoonDownloadRuntime.error(errorMsg = "解析结果为空")
            return
        }
        val mediaType = resolvedPlayerInfo.quickDownloadMediaType()
        if (mediaType == null || mediaType !in engine.descriptor.supportedMediaTypes) {
            cartoonDownloadRuntime.error(
                errorMsg = "${engine.descriptor.displayName} 不支持当前媒体类型"
            )
            return
        }

        engine.start(object : QuickDownloadEngineContext {
            override val taskId: String = cartoonDownloadRuntime.req.uuid
            override val request: CartoonDownloadReq = cartoonDownloadRuntime.req
            override val playerInfo = resolvedPlayerInfo

            private fun isCurrent(): Boolean {
                return !cartoonDownloadRuntime.isCanceled() &&
                    cartoonDownloadRuntime.currentAction === this@QuickDownloadAction
            }

            override fun report(progress: Float, status: String, detail: String) {
                if (isCurrent()) {
                    cartoonDownloadRuntime.dispatchToBus(
                        process = progress.coerceIn(-1f, 1f),
                        status = status,
                        subStatus = detail,
                    )
                }
            }

            override fun complete(artifact: QuickDownloadArtifact) {
                synchronized(cartoonDownloadRuntime.lock) {
                    if (!isCurrent()) return
                    cartoonDownloadRuntime.quickDownloadArtifact = artifact
                    cartoonDownloadRuntime.stepCompletely(this@QuickDownloadAction)
                }
            }

            override fun fail(error: Throwable?, message: String) {
                synchronized(cartoonDownloadRuntime.lock) {
                    if (!isCurrent()) return
                    cartoonDownloadRuntime.error(error = error, errorMsg = message)
                }
            }
        })
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        engineRegistry.find(cartoonDownloadRuntime.req.quickDownloadEngineId)
            ?.cancel(cartoonDownloadRuntime.req.uuid)
    }

    override fun onTaskCompletely(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        engineRegistry.find(cartoonDownloadRuntime.req.quickDownloadEngineId)
            ?.clear(cartoonDownloadRuntime.req.uuid)
    }
}
