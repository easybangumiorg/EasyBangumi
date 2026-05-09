package org.easybangumi.next.shared.download.action

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.download.model.DownloadRuntime
import org.easybangumi.next.shared.source.SourceCase

/**
 * 解析播放地址
 */
class ParseAction(
    private val sourceCase: SourceCase,
) : DownloadAction {

    override val name = DownloadChain.ACTION_PARSE

    override fun isAsync() = false

    override suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean = false

    override suspend fun execute(runtime: DownloadRuntime) {
        runtime.reportStatus("解析中...")

        val req = runtime.req
        val cartoonIndex = CartoonIndex(id = req.fromCartoonId, source = req.fromCartoonSource)
        val playerLine = PlayerLine(id = req.fromPlayLineId, label = req.fromPlayLineLabel, episodeList = emptyList())
        val episode = Episode(id = req.fromEpisodeId, label = req.fromEpisodeLabel, order = req.fromEpisodeOrder)

        val playBusinessResp = sourceCase.playBusinessFlow().first()
        val playBusiness = playBusinessResp.businessList
            .find { it.source.key == req.fromCartoonSource }
            ?: throw IllegalStateException("找不到源: ${req.fromCartoonSource}")

        var lastError: Throwable? = null
        repeat(3) { attempt ->
            if (runtime.isCanceled) return

            try {
                val result = withTimeoutOrNull(50_000) {
                    playBusiness.async { scope ->
                        getPlayInfo(cartoonIndex, playerLine, episode)
                    }.await()
                } ?: throw IllegalStateException("解析超时")

                if (result.isOk()) {
                    val playInfo = result.okOrNull()!!
                    if (playInfo.url.isEmpty()) {
                        throw IllegalStateException("播放地址为空")
                    }
                    runtime.playerInfo = playInfo
                    runtime.stepComplete()
                    return
                } else if (result.isError()) {
                    lastError = IllegalStateException((result as DataState.Error).errorMsg)
                    runtime.retryCount = attempt + 1
                }
            } catch (e: Exception) {
                lastError = e
                runtime.retryCount = attempt + 1
            }
        }

        throw lastError ?: IllegalStateException("解析失败")
    }

    override suspend fun pause(runtime: DownloadRuntime): Boolean = false
    override suspend fun resume(runtime: DownloadRuntime): Boolean = false
    override suspend fun cancel(runtime: DownloadRuntime) {}
    override suspend fun onTaskComplete(runtime: DownloadRuntime) {}
}
