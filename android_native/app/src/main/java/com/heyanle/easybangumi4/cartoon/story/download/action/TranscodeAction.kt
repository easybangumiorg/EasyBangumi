package com.heyanle.easybangumi4.cartoon.story.download.action

import android.app.Application
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.cartoon.story.download.utils.M3U8Utils
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.EasyMemoryInfo
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import com.jeffmony.m3u8library.VideoProcessManager
import com.jeffmony.m3u8library.listener.IVideoTransformListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

/**
 * Created by heyanle on 2024/8/4.
 * https://github.com/heyanLE
 */
class TranscodeAction(
    private val application: Application
) : BaseAction {

    companion object {
        const val NAME = "Transcode"
    }

    private val scope = MainScope()
    // 该阶段同时只能处理一个任务
    private val executor = CoroutineProvider.newSingleExecutor
    private val cacheFolder = application.getCachePath("transcode")

    private val decryptCacheFolder = File(cacheFolder, "decrypt")
    private val ffmpegCacheFolder = File(cacheFolder, "ffmpeg")


    inner class TranscodeRunnable(
        private val runtime: CartoonDownloadRuntime
    ) : Runnable {
        override fun run() {
            if (runtime.isCanceled() || runtime.isError()) {
                return
            }
            innerRun(runtime)
        }
    }


    private fun innerRun(
        cartoonDownloadRuntime: CartoonDownloadRuntime
    ) {
        synchronized(cartoonDownloadRuntime.lock) {
            try {
                if (!decrypt(cartoonDownloadRuntime)) {
                    cartoonDownloadRuntime.error(
                        errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error),
                        error = IOException("Decrypt failed")
                    )
                    return
                }
                if (
                    !ffmpeg(
                        cartoonDownloadRuntime,
                        // 回调为异步，需要重新获取锁
                        onCompletely = {
                            synchronized(cartoonDownloadRuntime.lock) {
                                cartoonDownloadRuntime.filePathBeforeCopy =
                                    cartoonDownloadRuntime.ffmpegFile?.absolutePath ?: ""
                                cartoonDownloadRuntime.stepCompletely(this)
                            }
                        },
                        onError = {
                            synchronized(cartoonDownloadRuntime.lock) {
                                cartoonDownloadRuntime.error(
                                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.transcode_error),
                                    error = it
                                )
                            }
                        }
                    )
                ) {
                    cartoonDownloadRuntime.error(
                        errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error),
                        error = IOException("Decrypt failed")
                    )
                    return
                }
            }catch (e: Throwable){
                cartoonDownloadRuntime.error(
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.transcode_error),
                    error = e
                )
            }

        }

    }

    override suspend fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean {
        // 文件最终是改名，只要存在就一定已完成
        val realTarget = File(cacheFolder, "${cartoonDownloadReq.uuid}.mp4")
        return realTarget.exists() && realTarget.isFile && realTarget.canRead() && realTarget.length() > 0
    }

    // 不支持暂停
    override suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        return false
    }

    override fun push(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        val realTarget = File(cacheFolder, "${cartoonDownloadRuntime.req.uuid}.mp4")
        if ( realTarget.exists() && realTarget.isFile && realTarget.canRead() && realTarget.length() > 0) {
            cartoonDownloadRuntime.filePathBeforeCopy = realTarget.absolutePath
            cartoonDownloadRuntime.stepCompletely(this)
            return
        }


        // 非 m3u8 任务不需要转码
        if (cartoonDownloadRuntime.m3u8Entity == null) {
            synchronized(cartoonDownloadRuntime.lock) {
                cartoonDownloadRuntime.filePathBeforeCopy =
                    cartoonDownloadRuntime.ariaDownloadFilePath
                cartoonDownloadRuntime.stepCompletely(this)
            }
            return
        }
        val runnable = TranscodeRunnable(cartoonDownloadRuntime)
        synchronized(cartoonDownloadRuntime.lock) {
            cartoonDownloadRuntime.transcodeRunnable = runnable
        }
        executor.execute(runnable)
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        executor.remove(cartoonDownloadRuntime.transcodeRunnable)
        try {
            M3U8Utils.deleteM3U8WithTs(
                cartoonDownloadRuntime.decryptFile?.absolutePath ?: ""
            )
            M3U8Utils.deleteM3U8WithTs(
                cartoonDownloadRuntime.decryptCacheFile?.absolutePath ?: ""
            )
            cartoonDownloadRuntime.ffmpegCacheFile?.delete()
            cartoonDownloadRuntime.decryptCacheFile?.delete()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    /**
     * 最终生成文件路径 File(cacheFolder, "${cartoonDownloadRuntime.req.uuid}.m3u8")
     */
    private fun decrypt(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        val entity = cartoonDownloadRuntime.m3u8Entity ?: return false
        val localM3U8 = File(entity.filePath)

        if (!localM3U8.exists() || !localM3U8.canRead()) {
            return false
        }
        val target = File(decryptCacheFolder, "${cartoonDownloadRuntime.req.uuid}.m3u8.temp")
        val realTarget = File(decryptCacheFolder, "${cartoonDownloadRuntime.req.uuid}.m3u8")
        decryptCacheFolder.mkdirs()
        target.delete()
        if (!target.createNewFile() || !target.canWrite()) {
            return false
        }
        target.deleteOnExit()
        // 这里改名是原子操作，只要文件存在就一定成功
        cartoonDownloadRuntime.decryptFile = realTarget
        scope.launch {
            cartoonDownloadRuntime.dispatchToBus(
                -1f,
                stringRes(com.heyanle.easy_i18n.R.string.decrypting),
            )
        }
        // 将本地 m3u8 文件中的 ts 全都解密改写成 tsh 文件
        // 输出新的 m3u8 文件，去除 key 标签，文件路径改为 tsh
        // 如果 tsh 文件的文件头是 png，则去除文件头
        val it = localM3U8.readLines().iterator()
        val writer = target.writer(Charsets.UTF_8).buffered()
        val tsFiles = arrayListOf<File>()
        val targetTsFiles = arrayListOf<File>()
        val memoryInfo = EasyMemoryInfo(application)
        memoryInfo.update()
        // 内存不足直接跳过
        if (memoryInfo.lowMemory) {
            realTarget.delete()
            localM3U8.renameTo(realTarget)
            return true
        }
        try {
            while (it.hasNext()) {
                val line = it.next()
                if (line.startsWith("#EXTINF")) {
                    if (!it.hasNext()) {
                        return false
                    } else {
                        val ts = it.next()
                        val file = File(ts)
                        val targetFile = File("${ts}h")
                        // 如果大于当前可用内存 80% 以上就不解了直接丢 ffmpeg 然后祈祷他没有改文件头
                        memoryInfo.update()
                        if (memoryInfo.availMem * 0.8 <= file.length()
                            || (Runtime.getRuntime()?.freeMemory()
                                ?: Long.MAX_VALUE) * 0.8 <= file.length()
                        ) {
                            realTarget.delete()
                            localM3U8.renameTo(realTarget)
                            return true
                        }
                        tsFiles.add(file)
                        // ts -> tsh
                        targetTsFiles.add(targetFile)
                        writer.write(line)
                        writer.newLine()
                        writer.write(targetFile.absolutePath)
                        writer.newLine()
                    }
                } else if (line.startsWith("#EXT-X-KEY")) {
                    // 新的 m3u8 文件不用解密了
                    continue
                } else {
                    writer.write(line)
                    writer.newLine()
                }
            }
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            // oom 了 也放弃解密
            realTarget.delete()
            localM3U8.renameTo(realTarget)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            // 解密失败
            return false
        }
        writer.flush()

        // 开始解密咯！
        val needDecrypt = !entity.method.isNullOrEmpty()
        val keyFile = File(entity.filePath)
        val keyB = keyFile.readBytes()

        scope.launch {
            cartoonDownloadRuntime.dispatchToBus(
                -1f,
                stringRes(com.heyanle.easy_i18n.R.string.decrypting),
                "0/${tsFiles.size}"
            )
        }
        for (i in 0 until tsFiles.size.coerceAtMost(targetTsFiles.size)) {
            scope.launch {
                cartoonDownloadRuntime.dispatchToBus(
                    if (tsFiles.size == 0) 0f else {
                        (i + 1) / (tsFiles.size).toFloat()
                    },
                    stringRes(com.heyanle.easy_i18n.R.string.decrypting),
                    "${i + 1}/${tsFiles.size}"
                )
            }

            val ts = tsFiles[i]
            val tsh = targetTsFiles[i]
            val parent = tsh.parentFile ?: return false

            val tshTemp = File(parent, tsh.name + ".temp")
            // ts 文件不在 tsh && 文件存在 && tsh.temp 文件不存在这说明该文件解密过了，直接跳过
            if (!ts.exists() && tsh.exists() && !tshTemp.exists()) {
                continue
            }
            tsh.delete()
            tshTemp.delete()
            tshTemp.createNewFile()
            if (!tshTemp.canRead() || !tshTemp.canWrite()) {
                return false
            }
            val s = ts.readBytes()
            val res = if (needDecrypt) M3U8Utils.decrypt(
                s,
                s.size,
                keyB,
                entity.iv,
                entity.method
            ) else s
            // 文件头伪装成 png
            val rr = res ?: s
            if (rr.size >= 4) {
                if (rr[0].toInt() == 0x89 && rr[1].toInt() == 0x50 && rr[2].toInt() == 0x4E && rr[3].toInt() == 0x47) {
                    rr[0] = 0xff.toByte()
                    rr[1] = 0xff.toByte()
                    rr[2] = 0xff.toByte()
                    rr[3] = 0xff.toByte()
                }
            }
            tshTemp.writeBytes(rr)
            tshTemp.renameTo(tsh)
            ts.delete()
        }
        target.renameTo(realTarget)
        return true
    }

    private fun ffmpeg(
        cartoonDownloadRuntime: CartoonDownloadRuntime,
        onError: (Exception?) -> Unit,
        onCompletely: () -> Unit
    ): Boolean {
        val m3u8 = cartoonDownloadRuntime.decryptFile
        if (m3u8 == null || !m3u8.exists() || !m3u8.canRead()) {
            return false
        }
        val realTarget = File(ffmpegCacheFolder, "${cartoonDownloadRuntime.req.uuid}.mp4")
        ffmpegCacheFolder.mkdirs()
        cartoonDownloadRuntime.ffmpegFile = realTarget
        val target = File(ffmpegCacheFolder, realTarget.name + ".temp.mp4")
        scope.launch {
            cartoonDownloadRuntime.dispatchToBus(
                0f,
                stringRes(com.heyanle.easy_i18n.R.string.transcoding)
            )
        }

        VideoProcessManager.getInstance().transformM3U8ToMp4(
            m3u8.absolutePath,
            target.absolutePath,
            object : IVideoTransformListener {
                override fun onTransformProgress(progress: Float) {
                    scope.launch {
                        cartoonDownloadRuntime.dispatchToBus(
                            progress,
                            stringRes(com.heyanle.easy_i18n.R.string.transcoding),
                            "${(progress).toInt()}%"
                        )
                    }
                }

                override fun onTransformFailed(e: Exception?) {
                    onError(e)

                }

                override fun onTransformFinished() {
                    target.renameTo(realTarget)
                    M3U8Utils.deleteM3U8WithTs(m3u8.absolutePath)
                    onCompletely()
                }
            }
        )
        return true
    }

}