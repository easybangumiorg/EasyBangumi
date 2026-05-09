package org.easybangumi.next.shared.download.model

import kotlinx.serialization.Serializable
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import kotlin.concurrent.Volatile

/**
 * 下载状态
 */
enum class DownloadState {
    WAITING,        // 等待中
    DOING,          // 下载中
    PAUSED,         // 已暂停
    STEP_COMPLETE,  // 当前步骤完成
    SUCCESS,        // 全部完成
    ERROR,          // 出错
    CANCEL,         // 已取消
}

/**
 * 下载请求（持久化）
 */
@Serializable
data class DownloadReq(
    val uuid: String,
    val fromCartoonId: String,
    val fromCartoonSource: String,
    val fromCartoonName: String,
    val fromPlayLineId: String,
    val fromPlayLineLabel: String,
    val fromEpisodeId: String,
    val fromEpisodeLabel: String,
    val fromEpisodeOrder: Int,
    val toLocalItemId: String,
    val toEpisodeTitle: String,
    val toEpisode: Int,
    val stepChain: List<String>,
)

/**
 * 下载运行时状态（不持久化）
 */
data class DownloadRuntime(
    val req: DownloadReq,
) {
    @Volatile
    var state: DownloadState = DownloadState.WAITING

    @Volatile
    var currentStepIndex: Int = 0

    // ParseAction 结果
    @Volatile
    var playerInfo: PlayInfo? = null

    // HTTP 下载结果
    @Volatile
    var cacheFilePath: String? = null

    // M3U8 下载（断点续传用）
    @Volatile
    var cacheDir: String? = null

    @Volatile
    var downloadedSegments: Int = 0

    // 进度
    @Volatile
    var progress: Float = -1f

    @Volatile
    var statusText: String = ""

    @Volatile
    var subStatusText: String = ""

    // 控制
    @Volatile
    var isPaused: Boolean = false

    @Volatile
    var isCanceled: Boolean = false

    // 重试计数
    @Volatile
    var retryCount: Int = 0

    // 错误信息
    @Volatile
    var errorMessage: String? = null

    @Volatile
    var error: Throwable? = null

    /**
     * 报告进度
     */
    fun reportProgress(progress: Float, status: String, subStatus: String? = null) {
        this.progress = progress
        this.statusText = status
        if (subStatus != null) {
            this.subStatusText = subStatus
        }
    }

    /**
     * 报告状态
     */
    fun reportStatus(status: String) {
        this.statusText = status
    }

    /**
     * 报告错误
     */
    fun reportError(error: Throwable, message: String? = null) {
        this.state = DownloadState.ERROR
        this.error = error
        this.errorMessage = message ?: error.message
    }

    /**
     * 标记当前步骤完成
     */
    fun stepComplete() {
        this.state = DownloadState.STEP_COMPLETE
    }

    /**
     * 标记全部完成
     */
    fun complete() {
        this.state = DownloadState.SUCCESS
        this.progress = 1f
        this.statusText = "完成"
    }

    /**
     * 标记暂停
     */
    fun markPaused() {
        this.state = DownloadState.PAUSED
        this.isPaused = true
        this.statusText = "已暂停"
    }

    /**
     * 标记恢复
     */
    fun markResumed() {
        this.state = DownloadState.DOING
        this.isPaused = false
        this.statusText = "下载中"
    }

    /**
     * 标记取消
     */
    fun markCanceled() {
        this.state = DownloadState.CANCEL
        this.isCanceled = true
    }
}

/**
 * 下载信息（请求 + 运行时）
 */
data class DownloadInfo(
    val req: DownloadReq,
    val runtime: DownloadRuntime? = null,
) {
    val isActive: Boolean
        get() = runtime != null && runtime.state in listOf(
            DownloadState.WAITING,
            DownloadState.DOING,
            DownloadState.PAUSED,
            DownloadState.STEP_COMPLETE
        )

    val isComplete: Boolean
        get() = runtime?.state == DownloadState.SUCCESS

    val isError: Boolean
        get() = runtime?.state == DownloadState.ERROR
}
