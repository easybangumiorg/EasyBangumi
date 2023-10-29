package com.heyanle.easybangumi4.download.step

import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadTask
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import java.io.File
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

/**
 * Created by heyanlin on 2023/10/2.
 */
class AriaStep(
    private val downloadController: DownloadController,
    private val downloadBus: DownloadBus,
) : BaseStep, DownloadTaskListener {

    companion object {
        const val NAME = "aria"
    }

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

    override fun init(downloadItem: DownloadItem): DownloadItem? {
        val entity = aria.getDownloadEntity(downloadItem.bundle.ariaId)
            ?: return downloadItem.copy(
                state = 0,
            )
        return when(entity.state){
            IEntity.STATE_COMPLETE -> downloadItem.copy(state = 2)
            IEntity.STATE_CANCEL -> null
            IEntity.STATE_FAIL -> downloadItem.copy(
                state = -1,
                errorMsg = ""
            )
            else -> {
                aria.load(downloadItem.bundle.ariaId).ignoreCheckPermissions().resume()
                downloadItem.copy(state = 1)
            }
        }
    }
    override fun invoke(downloadItem: DownloadItem) {
        val entity = aria.getDownloadEntity(downloadItem.bundle.ariaId)
        if (entity != null) {
            val info = downloadBus.getInfo(downloadItem.uuid)
            if (info.status.value.isEmpty() && info.subStatus.value.isEmpty()) {
                if (entity.state == IEntity.STATE_STOP) {
                    aria.load(downloadItem.bundle.ariaId).ignoreCheckPermissions().resume()
                }
            }
            return
        }
        val info = downloadItem.bundle.playerInfo
        if (info == null) {
            error(downloadItem.uuid, stringRes(R.string.download_error))
            return
        }
        when (info.decodeType) {
            PlayerInfo.DECODE_TYPE_OTHER -> {
                val path = File(downloadItem.bundle.downloadFolder, downloadItem.bundle.downloadFileName).absolutePath
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
            }

            PlayerInfo.DECODE_TYPE_HLS -> {
                val path = File(downloadItem.bundle.downloadFolder, downloadItem.bundle.downloadFileName).absolutePath
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
            }

            else -> {
                error(downloadItem.uuid, stringRes(com.heyanle.easy_i18n.R.string.download_error))
            }
        }
    }

    override fun onClick(downloadItem: DownloadItem): Boolean {
        val entity = aria.getDownloadEntity(downloadItem.bundle.ariaId)
            ?: return false
        when(entity.state){
            IEntity.STATE_RUNNING, IEntity.STATE_WAIT -> {
                aria.load(downloadItem.bundle.ariaId).ignoreCheckPermissions().stop()
            }
            IEntity.STATE_STOP -> {
                aria.load(downloadItem.bundle.ariaId).ignoreCheckPermissions().resume()
            }
            else -> return false
        }
        return true
    }


    override fun onRemove(downloadItem: DownloadItem) {
        aria.load(downloadItem.bundle.ariaId)?.ignoreCheckPermissions()?.cancel(true)
        downloadController.updateDownloadItem(downloadItem.uuid){
            it.copy(isRemoved = true)
        }
    }

    private fun pushCompletely(downloadItem: DownloadItem, taskId: Long) {
        if(taskId == -1L){
            error(downloadItem.uuid, stringRes(R.string.download_create_failed))
            return
        }
        downloadController.updateDownloadItem(downloadItem.uuid) {
            it.copy(
                bundle = it.bundle.apply {
                    ariaId = taskId
                }
            )
        }
    }

    private fun downloadCompletely(uuid: String, task: DownloadTask) {
        downloadController.updateDownloadItem(uuid) {
            it.copy(
                state = 2,
                bundle = it.bundle.apply {
                    m3U8Entity = task.entity.m3U8Entity
                }
            )
        }
    }

    private fun error(uuid: String, error: String) {
        downloadController.updateDownloadItem(uuid) {
            it.copy(
                state = -1,
                errorMsg = error,
            )
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
        task?.let { t ->
            t.extendField?.let { uuid ->
                val info = downloadBus.getInfo(uuid)
                info.status.value = stringRes(com.heyanle.easy_i18n.R.string.waiting)
                info.process.value = if ((t.entity.fileSize) <= 0L) -1f else ((t.entity.percent) / 100f)
                info.subStatus.value =
                    if (t.entity.fileSize > 0L) t.convertSpeed?:"" else t.convertCurrentProgress ?:""

            }
        }
    }

    override fun onPre(task: DownloadTask?) {
        //onTaskRunning(task)
    }

    override fun onTaskPre(task: DownloadTask?) {
        //onTaskRunning(task)
    }

    override fun onTaskResume(task: DownloadTask?) {
        onTaskRunning(task)
    }

    override fun onTaskStart(task: DownloadTask?) {
        //onTaskRunning(task)
    }

    override fun onTaskStop(task: DownloadTask?) {
        task?.let { t ->
            t.extendField?.let { uuid ->
                val info = downloadBus.getInfo(uuid)
                info.status.value = stringRes(com.heyanle.easy_i18n.R.string.pausing)
                info.process.value = if (t.entity.fileSize <= 0L) -1f else t.entity.percent / 100f
                info.subStatus.value =
                    if (t.entity.fileSize > 0L) t.convertSpeed else t.convertCurrentProgress

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
                info.subStatus.value = t.convertSpeed ?: ""
            }
        }
    }

    override fun onNoSupportBreakPoint(task: DownloadTask?) {
        stringRes(com.heyanle.easy_i18n.R.string.no_support_break_point).moeSnackBar()
    }
}