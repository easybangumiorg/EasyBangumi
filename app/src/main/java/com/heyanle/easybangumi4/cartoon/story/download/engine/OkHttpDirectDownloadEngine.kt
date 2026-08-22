package com.heyanle.easybangumi4.cartoon.story.download.engine

import android.app.Application
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.utils.OkhttpHelper
import com.heyanle.easybangumi4.utils.getCachePath
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 独立于 Aria 的直链快速下载引擎。支持 HTTP Range 断点与暂停/继续；
 * HLS 由能力协商拒绝，避免用不完整实现处理复杂清单。
 */
class OkHttpDirectDownloadEngine(
    application: Application,
) : QuickDownloadEngine {

    override val descriptor = QuickDownloadEngineDescriptor(
        id = QuickDownloadEngineIds.OKHTTP_DIRECT,
        displayName = "OkHttp（仅直链）",
        supportedMediaTypes = setOf(QuickDownloadMediaType.DIRECT),
    )

    private val cacheFolder = File(application.getCachePath("okhttp_download"))
    private val sessions = ConcurrentHashMap<String, Session>()

    private class Session(
        val context: QuickDownloadEngineContext,
        val paused: AtomicBoolean = AtomicBoolean(false),
        val canceled: AtomicBoolean = AtomicBoolean(false),
        @Volatile var call: Call? = null,
    )

    override suspend fun canResume(request: CartoonDownloadReq): Boolean {
        return finalFile(request.uuid).isUsable() || partialFile(request.uuid).let {
            it.isFile && it.length() > 0
        }
    }

    override suspend fun toggle(taskId: String): QuickDownloadToggleResult {
        val session = sessions[taskId] ?: return QuickDownloadToggleResult.UNSUPPORTED
        return if (session.paused.compareAndSet(false, true)) {
            session.call?.cancel()
            session.context.report(-1f, "已暂停")
            QuickDownloadToggleResult.PAUSED
        } else if (session.paused.compareAndSet(true, false)) {
            begin(session)
            QuickDownloadToggleResult.RESUMED
        } else {
            QuickDownloadToggleResult.UNSUPPORTED
        }
    }

    override fun start(context: QuickDownloadEngineContext) {
        cacheFolder.mkdirs()
        val completed = finalFile(context.taskId)
        if (completed.isUsable()) {
            context.complete(QuickDownloadArtifact.DirectFile(completed.absolutePath))
            return
        }
        val session = Session(context)
        sessions.put(context.taskId, session)?.run {
            canceled.set(true)
            call?.cancel()
        }
        begin(session)
    }

    override fun cancel(taskId: String) {
        sessions.remove(taskId)?.run {
            canceled.set(true)
            call?.cancel()
        }
        partialFile(taskId).delete()
        finalFile(taskId).delete()
    }

    override fun clear(taskId: String) {
        sessions.remove(taskId)
    }

    private fun begin(session: Session) {
        if (session.canceled.get() || session.paused.get()) return
        val context = session.context
        val partial = partialFile(context.taskId)
        val downloadedBytes = partial.takeIf { it.isFile }?.length() ?: 0L
        val request = Request.Builder()
            .url(context.playerInfo.uri)
            .apply {
                context.playerInfo.header?.forEach { (name, value) -> header(name, value) }
                if (downloadedBytes > 0) header("Range", "bytes=$downloadedBytes-")
            }
            .build()
        context.report(-1f, "连接中")
        val call = OkhttpHelper.client.newCall(request)
        session.call = call
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (session.canceled.get() || sessions[context.taskId] !== session) return
                if (session.paused.get()) {
                    context.report(-1f, "已暂停")
                } else {
                    sessions.remove(context.taskId, session)
                    context.fail(e, e.message ?: "下载失败")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        sessions.remove(context.taskId, session)
                        context.fail(null, "下载失败：HTTP ${response.code}")
                        return
                    }
                    val append = downloadedBytes > 0 && response.code == 206
                    val startBytes = if (append) downloadedBytes else 0L
                    val body = response.body
                    val remaining = body?.contentLength() ?: -1L
                    val totalBytes = if (remaining >= 0) startBytes + remaining else -1L
                    if (body == null) {
                        sessions.remove(context.taskId, session)
                        context.fail(null, "下载响应为空")
                        return
                    }
                    partial.parentFile?.mkdirs()
                    val startedAt = System.nanoTime()
                    val written: Long
                    try {
                        written = sourceAndWrite(
                            response = response,
                            target = partial,
                            append = append,
                            session = session,
                            startBytes = startBytes,
                            totalBytes = totalBytes,
                            startedAt = startedAt,
                        )
                    } catch (e: IOException) {
                        if (!session.paused.get() && !session.canceled.get()) {
                            sessions.remove(context.taskId, session)
                            context.fail(e, e.message ?: "写入下载文件失败")
                        }
                        return
                    }
                    if (session.paused.get() || session.canceled.get() ||
                        sessions[context.taskId] !== session
                    ) {
                        return
                    }
                    if (totalBytes > 0 && written < totalBytes) {
                        sessions.remove(context.taskId, session)
                        context.fail(null, "下载文件不完整")
                        return
                    }
                    val completed = finalFile(context.taskId)
                    completed.delete()
                    if (!partial.renameTo(completed) || !completed.isUsable()) {
                        sessions.remove(context.taskId, session)
                        context.fail(null, "完成下载文件失败")
                        return
                    }
                    sessions.remove(context.taskId, session)
                    context.complete(QuickDownloadArtifact.DirectFile(completed.absolutePath))
                }
            }
        })
    }

    private fun sourceAndWrite(
        response: Response,
        target: File,
        append: Boolean,
        session: Session,
        startBytes: Long,
        totalBytes: Long,
        startedAt: Long,
    ): Long {
        val body = response.body ?: throw IOException("Response body is empty")
        var written = startBytes
        body.byteStream().use { input ->
            java.io.FileOutputStream(target, append).buffered().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    if (session.paused.get() || session.canceled.get()) return written
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                    written += count
                    val progress = if (totalBytes > 0) written.toFloat() / totalBytes else -1f
                    val seconds = ((System.nanoTime() - startedAt) / 1_000_000_000.0)
                        .coerceAtLeast(0.001)
                    val speed = ((written - startBytes) / seconds).toLong()
                    session.context.report(progress, "下载中", formatSpeed(speed))
                }
                output.flush()
            }
        }
        return written
    }

    private fun formatSpeed(bytesPerSecond: Long): String = when {
        bytesPerSecond >= 1024 * 1024 -> "%.1f MB/s".format(bytesPerSecond / 1024.0 / 1024.0)
        bytesPerSecond >= 1024 -> "%.1f KB/s".format(bytesPerSecond / 1024.0)
        else -> "$bytesPerSecond B/s"
    }

    private fun partialFile(taskId: String) = File(cacheFolder, "$taskId.mp4.part")
    private fun finalFile(taskId: String) = File(cacheFolder, "$taskId.mp4")

    private fun File.isUsable(): Boolean = isFile && canRead() && length() > 0
}
