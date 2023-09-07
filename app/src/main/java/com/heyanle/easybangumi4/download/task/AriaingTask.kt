package com.heyanle.easybangumi4.download.task

import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.inf.IEntity
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.download.DownloadBundle
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.coroutines.resume

/**
 * Created by HeYanLe on 2023/9/3 23:22.
 * https://github.com/heyanLE
 */
class AriaingTask(
    private val cache: File
) : DownloadTask, DownloadTaskListener {

    private val aria = Aria.download(this)

    init {
        aria.register()
    }

    private val m3u8Option = M3U8VodOption().apply {
        setVodTsUrlConvert { m3u8Url, tsUrls ->
            val list = arrayListOf<String>()
            val pattern = "[0-9a-zA-Z]+[.]ts"
            val r = Pattern.compile(pattern)
            for (i in tsUrls.indices) {
                val tspath = tsUrls[i]
                if (tspath.startsWith("http://") || tspath.startsWith("https://")) {
                    list.add(tspath)
                } else if (r.matcher(tspath).find()) {
                    val e = m3u8Url.lastIndexOf("/") + 1
                    list.add(m3u8Url.substring(0, e) + tspath)
                } else {
                    val host = URI(m3u8Url).host
                    list.add("$host/$tspath")
                }
            }
            list
        }
        setBandWidthUrlConverter { m3u8Url, bandWidthUrl ->
            if (bandWidthUrl.startsWith("http://") || bandWidthUrl.startsWith("https://")) {
                bandWidthUrl
            } else {
                val url = URL(m3u8Url)
                url.protocol + "://" + url.host + ":" + url.port + "/" + bandWidthUrl
            }
        }
        setUseDefConvert(false)
        generateIndexFile()
    }

    //挂起点
    private val conMap = ConcurrentHashMap<Long, CancellableContinuation<Throwable?>>()
    override suspend fun invoke(downloadBundle: DownloadBundle) {
        if (downloadBundle.isAriaCompletely) {
            return
        }
        suspendCancellableCoroutine<Throwable?> {
            conMap[downloadBundle.id]?.cancel()
            conMap[downloadBundle.id] = it
            val lastTask = aria.load(downloadBundle.ariaId)
            var needNewTask = false
            if (lastTask != null) {
                if (downloadBundle.isWorking) {
                    // 其他状态应该正在执行中，不用管
                    when (lastTask.taskState) {
                        IEntity.STATE_CANCEL, IEntity.STATE_FAIL -> needNewTask = true
                        IEntity.STATE_STOP -> lastTask.resume()
                    }
                } else {
                    when (lastTask.taskState) {
                        // 取消或失败的不用管
                        IEntity.STATE_CANCEL, IEntity.STATE_FAIL -> {}
                        IEntity.STATE_STOP -> {}
                        else -> {
                            lastTask.stop()
                        }
                    }
                }
            } else {
                if (downloadBundle.isWorking) {
                    needNewTask = true
                }
            }
            if (needNewTask) {
                if(downloadBundle.playerInfoDecodeType.toInt() == PlayerInfo.DECODE_TYPE_HLS){
                    val target = File(cache, "${downloadBundle.id}.source.m3u8").absolutePath
                    val taskId = aria.load(downloadBundle.playerInfoUri)
                        .option(HttpOption().apply {
                            downloadBundle.playerInfoHeaderJson.jsonTo<Map<String, String>>()
                                .iterator()
                                .forEach {
                                    addHeader(it.key, it.value)
                                }
                        })
                        .setFilePath(target)
                        .m3u8VodOption(m3u8Option)
                        .ignoreCheckPermissions()
                        .create()

                    downloadBundle.ariaId = taskId
                    downloadBundle.ariaTargetPath = target
                }else if(downloadBundle.playerInfoDecodeType.toInt() == PlayerInfo.DECODE_TYPE_OTHER) {
                    val taskId = aria.load(downloadBundle.playerInfoUri)
                        .option(HttpOption().apply {
                            downloadBundle.playerInfoHeaderJson.jsonTo<Map<String, String>>()
                                .iterator()
                                .forEach {
                                    addHeader(it.key, it.value)
                                }
                        })
                        .setFilePath(downloadBundle.videoPath)
                        .ignoreCheckPermissions()
                        .create()
                    downloadBundle.ariaId = taskId
                    downloadBundle.ariaTargetPath = downloadBundle.videoPath
                }else{
                    it.resume(DownloadTask.TaskErrorException("不支持的流媒体形式"))
                }
            }
        }
    }

    override fun onWait(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onPre(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskPre(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskResume(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskStart(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskStop(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskCancel(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
        con(task)?.cancel()
    }

    override fun onTaskFail(task: com.arialyy.aria.core.task.DownloadTask?, e: Exception?) {
        //TODO("Not yet implemented")
        con(task)?.resume(DownloadTask.TaskErrorException(stringRes(com.heyanle.easy_i18n.R.string.download_error), e))
    }

    override fun onTaskComplete(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
        con(task)?.resume(null)
    }

    override fun onTaskRunning(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onNoSupportBreakPoint(task: com.arialyy.aria.core.task.DownloadTask?) {
        //TODO("Not yet implemented")
    }

    private fun con(task: com.arialyy.aria.core.task.DownloadTask?): CancellableContinuation<Throwable?>?{
        return task?.let { conMap[it.entity.id] }
    }
}