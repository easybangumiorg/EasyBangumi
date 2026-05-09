package org.easybangumi.next.shared.download.action

import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.shared.download.model.DownloadRuntime
import org.easybangumi.next.shared.local.LocalItemFactory
import org.easybangumi.next.shared.local.LocalPreference

/**
 * 复制视频到本地目录 + 生成 Kodi NFO 文件
 */
class CopyAndNfoAction(
    private val localPreference: LocalPreference,
) : DownloadAction {

    override val name = DownloadChain.ACTION_COPY_AND_NFO

    override fun isAsync() = false

    override suspend fun canResume(req: org.easybangumi.next.shared.download.model.DownloadReq): Boolean {
        return false
    }

    override suspend fun execute(runtime: DownloadRuntime) {
        runtime.reportStatus("复制中...")

        val cacheFilePath = runtime.cacheFilePath
            ?: throw IllegalStateException("cacheFilePath is null")

        val sourceFile = UniFileFactory.fromUFD(UFD(type = UFD.TYPE_JVM, uri = cacheFilePath))
            ?: throw IllegalStateException("缓存文件不存在: $cacheFilePath")

        if (!sourceFile.exists()) {
            throw IllegalStateException("缓存文件不存在: $cacheFilePath")
        }

        val targetFolder = localPreference.getLocalFolder()
            ?: throw IllegalStateException("本地目录不可用")

        val req = runtime.req

        val animeFolder = targetFolder.child(req.toLocalItemId)
            ?: targetFolder.createDirectory(req.toLocalItemId)?.let { targetFolder.child(req.toLocalItemId) }
            ?: throw IllegalStateException("无法创建番剧文件夹")

        val mediaName = "${req.toLocalItemId} ${req.toEpisodeTitle} S1E${req.toEpisode}"
        val mediaFileName = "$mediaName.mp4"

        val mediaFile = animeFolder.child(mediaFileName)
            ?: throw IllegalStateException("无法获取媒体文件引用")

        sourceFile.openSource().buffer().use { source ->
            mediaFile.openSink(false).buffer().use { sink ->
                sink.writeAll(source)
            }
        }

        val nfoContent = LocalItemFactory.buildEpisodeNfo(
            title = req.toEpisodeTitle,
            season = 1,
            episode = req.toEpisode,
        )
        val nfoFile = animeFolder.child("$mediaName.nfo")
        if (nfoFile != null) {
            nfoFile.openSink(false).buffer().use { sink ->
                sink.writeUtf8(nfoContent)
            }
        }

        sourceFile.delete()
        runtime.stepComplete()
    }

    override suspend fun pause(runtime: DownloadRuntime): Boolean {
        return false
    }

    override suspend fun resume(runtime: DownloadRuntime): Boolean {
        return false
    }

    override suspend fun cancel(runtime: DownloadRuntime) {
    }

    override suspend fun onTaskComplete(runtime: DownloadRuntime) {
        runtime.cacheFilePath?.let { path ->
            UniFileFactory.fromUFD(UFD(type = UFD.TYPE_JVM, uri = path))?.delete()
        }
    }
}
