package org.easybangumi.next.shared.download.platform.desktop

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.readBytes
import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.PathProvider
import org.easybangumi.next.shared.download.action.DownloadChain
import org.easybangumi.next.shared.download.action.DownloadAction
import org.easybangumi.next.shared.download.action.m3u8.M3u8Decryptor
import org.easybangumi.next.shared.download.action.m3u8.M3u8Parser
import org.easybangumi.next.shared.download.model.DownloadRuntime

/**
 * Desktop 端 M3U8 下载 + AES 解密 + FFmpeg 合并
 */
class FfmpegM3u8DownloadAction(
    private val httpClient: HttpClient,
    private val pathProvider: PathProvider,
) : DownloadAction {

    override val name = DownloadChain.ACTION_FFMPEG_M3U8

    override fun isAsync() = true

    override suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean {
        val cacheDir = getCacheDir(req.uuid)
        return cacheDir != null && cacheDir.exists()
    }

    override suspend fun execute(runtime: DownloadRuntime) {
        val playerInfo = runtime.playerInfo
            ?: throw IllegalStateException("playerInfo is null")

        if (!FfmpegMerger.isAvailable()) {
            throw IllegalStateException("系统未安装 FFmpeg，请先安装 FFmpeg")
        }

        val cacheDir = getCacheDir(runtime.req.uuid)
            ?: throw IllegalStateException("缓存目录不可用")

        runtime.reportStatus("解析 M3U8...")

        val playlist = M3u8Parser.parse(httpClient, playerInfo.url, playerInfo.header ?: emptyMap())
        val headers = playerInfo.header ?: emptyMap()

        val tsFiles = mutableListOf<UniFile>()

        for ((index, segment) in playlist.segments.withIndex()) {
            if (runtime.isCanceled) return
            if (runtime.isPaused) {
                runtime.markPaused()
                runtime.downloadedSegments = index
                return
            }

            val tsFileName = "seg_%05d.ts".format(index)
            val tsFile = cacheDir.child(tsFileName)

            if (tsFile == null || !tsFile.exists() || tsFile.length() == 0L) {
                val bytes = httpClient.prepareGet(segment.url) {
                    headers.forEach { (k, v) -> header(k, v) }
                }.execute { response ->
                    response.readBytes()
                }

                val decrypted = if (segment.encryption?.method == "AES-128" && segment.encryption.uri != null) {
                    val key = M3u8Decryptor.fetchKey(httpClient, segment.encryption.uri!!, headers)
                    val iv = segment.encryption.iv ?: M3u8Decryptor.generateIV(index)
                    M3u8Decryptor.decryptAes128Cbc(bytes, key, iv)
                } else {
                    bytes
                }

                val targetFile = cacheDir.child(tsFileName)
                    ?: throw IllegalStateException("无法创建分片文件")
                targetFile.openSink(false).buffer().use { sink ->
                    sink.write(decrypted)
                }
                tsFiles.add(targetFile)
            } else {
                tsFiles.add(tsFile)
            }

            val progress = (index + 1).toFloat() / playlist.segments.size
            runtime.reportProgress(progress * 0.8f, "下载分片", "${index + 1}/${playlist.segments.size}")
        }

        runtime.reportStatus("合并中...")

        val outputFileName = "output.mp4"
        val outputFilePath = "${cacheDir.getFilePath().ifEmpty { cacheDir.getUri() }}/$outputFileName"

        val tsFilePaths = tsFiles.map { it.getFilePath().ifEmpty { it.getUri() } }

        val success = FfmpegMerger.merge(tsFilePaths, outputFilePath) { progress ->
            runtime.reportProgress(0.8f + progress * 0.2f, "合并中")
        }

        if (!success) {
            throw IllegalStateException("FFmpeg 合并失败")
        }

        tsFiles.forEach { it.delete() }

        runtime.cacheFilePath = outputFilePath
        runtime.stepComplete()
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
        getCacheDir(runtime.req.uuid)?.delete()
    }

    override suspend fun onTaskComplete(runtime: DownloadRuntime) {
        getCacheDir(runtime.req.uuid)?.delete()
    }

    private fun getCacheDir(uuid: String): UniFile? {
        val ufd = pathProvider.getCachePath("download")
        val root = UniFileFactory.fromUFD(ufd) ?: return null
        return root.child(uuid) ?: root.createDirectory(uuid)?.let { root.child(uuid) }
    }
}
