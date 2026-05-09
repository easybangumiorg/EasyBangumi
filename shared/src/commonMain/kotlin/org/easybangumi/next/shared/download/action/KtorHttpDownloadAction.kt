package org.easybangumi.next.shared.download.action

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.PathProvider
import org.easybangumi.next.shared.download.model.DownloadRuntime

/**
 * 基于 Ktor 的普通 HTTP 下载
 */
class KtorHttpDownloadAction(
    private val httpClient: HttpClient,
    private val pathProvider: PathProvider,
) : DownloadAction {

    override val name = DownloadChain.ACTION_KTOR_HTTP

    override fun isAsync() = true

    override suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean {
        val cacheDir = getCacheDir()
        val cacheFile = cacheDir?.child("${req.uuid}.mp4")
        return cacheFile != null && cacheFile.exists() && cacheFile.length() > 0
    }

    override suspend fun execute(runtime: DownloadRuntime) {
        val playerInfo = runtime.playerInfo
            ?: throw IllegalStateException("playerInfo is null")

        val cacheDir = getCacheDir()
            ?: throw IllegalStateException("缓存目录不可用")

        val cacheFile = cacheDir.child("${runtime.req.uuid}.mp4")
        val tempFile = cacheDir.child("${runtime.req.uuid}.mp4.tmp")

        if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0) {
            runtime.cacheFilePath = cacheFile.getFilePath().ifEmpty { cacheFile.getUri() }
            runtime.stepComplete()
            return
        }

        var downloaded = if (tempFile != null && tempFile.exists()) tempFile.length() else 0L

        runtime.reportStatus("下载中")

        httpClient.prepareGet(playerInfo.url) {
            playerInfo.header?.forEach { (k, v) -> header(k, v) }
            if (downloaded > 0) {
                header("Range", "bytes=$downloaded-")
            }
        }.execute { response ->
            if (!response.status.isSuccess()) {
                throw IllegalStateException("HTTP ${response.status}")
            }

            val totalLength = response.headers["Content-Length"]?.toLongOrNull()?.let {
                it + downloaded
            } ?: -1L

            val channel = response.bodyAsChannel()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

            if (tempFile == null) {
                throw IllegalStateException("无法创建临时文件")
            }

            tempFile.openSink(downloaded > 0).buffer().use { sink ->
                while (!channel.isClosedForRead) {
                    if (runtime.isCanceled) return@execute
                    if (runtime.isPaused) {
                        runtime.markPaused()
                        return@execute
                    }

                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead == -1) break

                    sink.write(buffer, 0, bytesRead)
                    downloaded += bytesRead

                    val progress = if (totalLength > 0) {
                        downloaded.toFloat() / totalLength
                    } else {
                        -1f
                    }
                    runtime.reportProgress(progress, "下载中")
                }
            }

            if (tempFile.exists()) {
                tempFile.renameTo("${runtime.req.uuid}.mp4")
            }

            if (cacheFile != null && cacheFile.exists()) {
                runtime.cacheFilePath = cacheFile.getFilePath().ifEmpty { cacheFile.getUri() }
            }
            runtime.stepComplete()
        }
    }

    override suspend fun pause(runtime: DownloadRuntime): Boolean {
        runtime.markPaused()
        return true
    }

    override suspend fun resume(runtime: DownloadRuntime): Boolean {
        runtime.markResumed()
        execute(runtime)
        return true
    }

    override suspend fun cancel(runtime: DownloadRuntime) {
        runtime.markCanceled()
        getCacheDir()?.child("${runtime.req.uuid}.mp4.tmp")?.delete()
    }

    override suspend fun onTaskComplete(runtime: DownloadRuntime) {
        getCacheDir()?.child("${runtime.req.uuid}.mp4.tmp")?.delete()
    }

    private fun getCacheDir(): UniFile? {
        val ufd = pathProvider.getCachePath("download")
        val dir = UniFileFactory.fromUFD(ufd)
        if (dir != null && !dir.exists()) {
            dir.createDirectory()
        }
        return dir
    }
}
