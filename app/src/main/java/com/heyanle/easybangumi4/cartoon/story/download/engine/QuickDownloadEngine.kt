package com.heyanle.easybangumi4.cartoon.story.download.engine

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo

object QuickDownloadEngineIds {
    const val ARIA = "aria"
    const val OKHTTP_DIRECT = "okhttp-direct"
}

enum class QuickDownloadMediaType {
    DIRECT,
    HLS,
}

data class QuickDownloadEngineDescriptor(
    val id: String,
    val displayName: String,
    val supportedMediaTypes: Set<QuickDownloadMediaType>,
)

enum class QuickDownloadToggleResult {
    PAUSED,
    RESUMED,
    UNSUPPORTED,
}

sealed interface QuickDownloadArtifact {
    val filePath: String

    data class DirectFile(
        override val filePath: String,
    ) : QuickDownloadArtifact

    data class HlsBundle(
        override val filePath: String,
        val keyPath: String?,
        val method: String?,
        val iv: String?,
    ) : QuickDownloadArtifact
}

interface QuickDownloadEngineContext {
    val taskId: String
    val request: CartoonDownloadReq
    val playerInfo: PlayerInfo

    fun report(progress: Float, status: String, detail: String = "")
    fun complete(artifact: QuickDownloadArtifact)
    fun fail(error: Throwable?, message: String)
}

/**
 * 快速模式只替换传输阶段。实现不得发布最终本地剧集，也不得依赖 UI/runtime。
 */
interface QuickDownloadEngine {
    val descriptor: QuickDownloadEngineDescriptor

    suspend fun canResume(request: CartoonDownloadReq): Boolean
    suspend fun toggle(taskId: String): QuickDownloadToggleResult
    fun start(context: QuickDownloadEngineContext)
    fun cancel(taskId: String)
    fun clear(taskId: String) = Unit
}

fun PlayerInfo.quickDownloadMediaType(): QuickDownloadMediaType? = when (decodeType) {
    PlayerInfo.DECODE_TYPE_OTHER -> QuickDownloadMediaType.DIRECT
    PlayerInfo.DECODE_TYPE_HLS -> QuickDownloadMediaType.HLS
    else -> null
}
