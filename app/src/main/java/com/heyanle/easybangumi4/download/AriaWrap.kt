package com.heyanle.easybangumi4.download

import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.inf.IEntity.STATE_RUNNING
import com.arialyy.aria.core.inf.IEntity.STATE_STOP
import com.arialyy.aria.core.task.DownloadTask
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

/**
 * Created by HeYanLe on 2023/9/17 15:50.
 * https://github.com/heyanLE
 */
class AriaWrap(
    private val downloadBus: DownloadBus,
    private val baseDownloadController: BaseDownloadController
) : DownloadTaskListener {

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

    fun push(downloadItem: DownloadItem) {
        val info = downloadItem.playerInfo
        if (info == null) {
            error(downloadItem.uuid, stringRes(com.heyanle.easy_i18n.R.string.download_error))
            return
        }
        if (info.decodeType == PlayerInfo.DECODE_TYPE_OTHER) {
            val path = downloadItem.filePathWithoutSuffix + ".mp4"
            val taskId = aria.load(info.uri)
                .setExtendField(downloadItem.uuid)
                .option(HttpOption().apply {
                    info.header?.iterator()?.forEach {
                        addHeader(it.key, it.value)
                    }
                })
                .setFilePath(path)
                .ignoreCheckPermissions()
                .create()
            pushCompletely(downloadItem, taskId)
        } else if (info.decodeType == PlayerInfo.DECODE_TYPE_HLS) {
            val path = downloadItem.filePathWithoutSuffix + "aria.m3u8"
            val taskId = aria.load(info.uri)
                .setExtendField(downloadItem.uuid)
                .option(HttpOption().apply {
                    info.header?.iterator()?.forEach {
                        addHeader(it.key, it.value)
                    }
                })
                .setFilePath(path)
                .m3u8VodOption(m3u8Option)
                .ignoreCheckPermissions()
                .create()
            pushCompletely(downloadItem, taskId)
        } else {
            error(downloadItem.uuid, stringRes(com.heyanle.easy_i18n.R.string.download_error))
        }

    }


    fun click(downloadItem: DownloadItem){
        aria.load(downloadItem.ariaId)?.let {
            it.taskState.logi("AriaWrap")
            when(it.taskState){
                STATE_STOP -> {
                    resume(downloadItem)
                }
                STATE_RUNNING -> {
                    pause(downloadItem)
                }
            }

        }
    }

    fun pause(downloadItem: DownloadItem){
        aria.load(downloadItem.ariaId).ignoreCheckPermissions().stop()
    }

    fun resume(downloadItem: DownloadItem){
        aria.load(downloadItem.ariaId).ignoreCheckPermissions().resume()
    }


    private fun error(uuid: String, error: String) {
        baseDownloadController.updateDownloadItem {
            it.map {
                if (it.uuid != uuid) {
                    it
                } else {
                    it.copy(
                        state = -1,
                        errorMsg = error,
                    )
                }
            }
        }
    }

    private fun pushCompletely(downloadItem: DownloadItem, taskId: Long) {
        baseDownloadController.updateDownloadItem {
            it.map {
                if (it != downloadItem) {
                    it
                } else {
                    it.copy(
                        state = 2,
                        ariaId = taskId,
                    )
                }
            }
        }
    }

    private fun downloadCompletely(uuid: String, task: DownloadTask) {
        baseDownloadController.updateDownloadItem {
            it.map {
                if (it.uuid != uuid) {
                    it
                } else {
                    it.copy(
                        state = 3,
                        m3U8Entity = task.entity.m3U8Entity,
                    )
                }
            }
        }
    }


    override fun onWait(task: DownloadTask?) {
//        task?.let { t ->
//            t.extendField?.let { uuid ->
//                val info = downloadBus.getInfo(uuid)
//                info.status.value = ""
//                info.process.value = if(t.entity.fileSize > 0L) t.entity.percent/100f else -1f
//                info.subStatus.value = ""
//            }
//        }
        onTaskRunning(task)
    }

    override fun onPre(task: DownloadTask?) {
        onTaskRunning(task)
    }

    override fun onTaskPre(task: DownloadTask?) {
        onTaskRunning(task)
    }

    override fun onTaskResume(task: DownloadTask?) {
        onTaskRunning(task)
    }

    override fun onTaskStart(task: DownloadTask?) {
        onTaskRunning(task)
    }

    override fun onTaskStop(task: DownloadTask?) {
        task?.let { t ->
            t.extendField?.let { uuid ->
                val info = downloadBus.getInfo(uuid)
                info.status.value = stringRes(com.heyanle.easy_i18n.R.string.pausing)
                info.process.value =  if (t.entity.fileSize <= 0L) -1f else t.entity.percent / 100f
                info.subStatus.value = if(t.entity.fileSize > 0L) t.convertSpeed else t.convertCurrentProgress

            }
        }
    }

    override fun onTaskCancel(task: DownloadTask?) {
        task?.let { t ->
            t.extendField?.let { uuid ->
                downloadBus.getInfo(uuid)
            }
        }
    }

    override fun onTaskFail(task: DownloadTask?, e: Exception?) {
        task?.let { t ->
            t.extendField?.let { uuid ->
                error(uuid, e?.message ?: stringRes(com.heyanle.easy_i18n.R.string.download_error))
            }
        }
    }

    override fun onTaskComplete(task: DownloadTask?) {
        task?.let { t ->
            t.extendField?.let { uuid ->
                downloadCompletely(uuid, t)
            }
        }
    }

    override fun onTaskRunning(task: DownloadTask?) {
        task?.let { t ->
            t.extendField?.let { uuid ->
                val info = downloadBus.getInfo(uuid)
                info.status.value = stringRes(com.heyanle.easy_i18n.R.string.downloading)
                info.process.value = if (t.entity.fileSize <= 0L) -1f else t.entity.percent / 100f
                info.subStatus.value = t.convertSpeed?:""
            }
        }
    }

    override fun onNoSupportBreakPoint(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }
}