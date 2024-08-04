package com.heyanle.easybangumi4.cartoon.story.download.action

import android.app.Application
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.HttpOption
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.download.DownloadReceiver
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.orm.DbEntity
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

/**
 * Created by heyanle on 2024/8/3.
 * https://github.com/heyanLE
 */
class AriaAction(
    application: Application,
    downloadPreference: CartoonDownloadPreference
) : BaseAction, DownloadTaskListener {

    companion object {
        const val NAME = "AriaAction"
    }

    private val aria: DownloadReceiver by lazy {
        Aria.download(this@AriaAction).apply {
            register()
        }
    }

    private val ariaId2Runtime = ConcurrentHashMap<Long, CartoonDownloadRuntime>()
    private val downloadFolder = application.getCachePath("aria_download")

    init {
        Aria.init(application)
        Aria.get(application).apply {
            downloadConfig.apply {
                maxTaskNum = downloadPreference.downloadMaxCountPref.get().toInt()
                isConvertSpeed = true
            }
        }
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
                if (url.port == -1) {
                    url.protocol + "://" + url.host  + "/" + bandWidthUrl
                } else {
                    url.protocol + "://" + url.host + ":" + url.port + "/" + bandWidthUrl
                }
            }
        }
        setUseDefConvert(false)
        generateIndexFile()
    }

    // action
    override suspend fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean {
        return withContext(Dispatchers.IO) {
            val task = aria.getFirstTaskWithExt(cartoonDownloadReq.uuid) ?: return@withContext false
            if (task.isComplete) {
                return@withContext true
            }
            (task.state == DownloadEntity.STATE_WAIT ||
                    task.state == DownloadEntity.STATE_COMPLETE ||
                    task.state == DownloadEntity.STATE_POST_PRE ||
                    task.state == DownloadEntity.STATE_RUNNING ||
                    task.state == DownloadEntity.STATE_STOP).apply {
                        // 不能恢复直接删除
                        if (!this) {
                            aria.load(task.id).cancel(true)
                        }
            }
        }
    }


    override suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        val entity = aria.getDownloadEntity(cartoonDownloadRuntime.ariaId)
            ?: return false
        when(entity.state){
            IEntity.STATE_RUNNING, IEntity.STATE_WAIT -> {
                aria.load(cartoonDownloadRuntime.ariaId).ignoreCheckPermissions().stop()
            }
            IEntity.STATE_STOP -> {

                aria.load(cartoonDownloadRuntime.ariaId).ignoreCheckPermissions().resume()
            }
            else -> return false
        }
        return true
    }

    override fun push(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        "push aria action".logi("Action")
        val entity = aria.getFirstTaskWithExt(cartoonDownloadRuntime.req.uuid)

        File(downloadFolder).mkdirs()
        if (entity != null) {
            cartoonDownloadRuntime.ariaId = entity.id
            cartoonDownloadRuntime.m3u8Entity = entity.m3U8Entity
            cartoonDownloadRuntime.ariaDownloadFilePath = entity.filePath
            ariaId2Runtime[entity.id] = cartoonDownloadRuntime
            if (entity.state == IEntity.STATE_STOP) {
                aria.load(cartoonDownloadRuntime.ariaId).ignoreCheckPermissions().resume()
            } else if (entity.state == IEntity.STATE_COMPLETE) {
                cartoonDownloadRuntime.m3u8Entity = entity.m3U8Entity
                cartoonDownloadRuntime.ariaDownloadFilePath = entity.filePath
                cartoonDownloadRuntime.stepCompletely(this)
            }
        } else {
            val playerInfo = cartoonDownloadRuntime.playerInfo ?: throw IllegalStateException("playerInfo is null")
            when (playerInfo.decodeType) {
                PlayerInfo.DECODE_TYPE_OTHER -> {
                    val file = File(downloadFolder, cartoonDownloadRuntime.req.uuid + ".mp4")
                    val path = file.absolutePath
                    val taskId = aria.load(playerInfo.uri)
                        .setExtendField(cartoonDownloadRuntime.req.uuid)
                        .option(HttpOption().apply {
                            playerInfo.header?.iterator()?.forEach {
                                addHeader(it.key, it.value)
                            }
                        })
                        .setFilePath(path)
                        .ignoreCheckPermissions()
                        .ignoreFilePathOccupy()
                        .create()
                    cartoonDownloadRuntime.ariaId = taskId
                    if (taskId != -1L) {
                        ariaId2Runtime[taskId] = cartoonDownloadRuntime
                    }
                    // pushCompletely(downloadItem, taskId)
                }

                PlayerInfo.DECODE_TYPE_HLS -> {
                    val path = File(downloadFolder, cartoonDownloadRuntime.req.uuid).absolutePath
                    val taskId = aria.load(playerInfo.uri)
                        .setExtendField(cartoonDownloadRuntime.req.uuid)
                        .option(HttpOption().apply {
                            playerInfo.header?.iterator()?.forEach {
                                addHeader(it.key, it.value)
                            }
                        })
                        .setFilePath(path)
                        .m3u8VodOption(m3u8Option)
                        .ignoreFilePathOccupy()
                        .ignoreCheckPermissions()
                        .create()
                    cartoonDownloadRuntime.ariaId = taskId
                    if (taskId != -1L) {
                        ariaId2Runtime[taskId] = cartoonDownloadRuntime
                    }
                    // pushCompletely(downloadItem, taskId)
                }

                else -> {
                    throw IllegalStateException("unknown decodeType")
                    // error(downloadItem.uuid, stringRes(com.heyanle.easy_i18n.R.string.download_error))
                }
            }
            if (cartoonDownloadRuntime.ariaId == -1L) {
                throw IllegalStateException("new task error")
            }
        }
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        aria.load(cartoonDownloadRuntime.ariaId)?.cancel(true)
    }


    // aria callback
    override fun onWait(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val runtime = ariaId2Runtime[entity.id] ?: return
        runtime.dispatchProcessToBus(
            task,
            stringRes(com.heyanle.easy_i18n.R.string.waiting),
        )
    }

    override fun onPre(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskPre(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskResume(task: DownloadTask?) {
        onTaskRunning(task)
    }

    override fun onTaskStart(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskStop(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val runtime = ariaId2Runtime[entity.id] ?: return
        runtime.dispatchProcessToBus(
            task,
            stringRes(com.heyanle.easy_i18n.R.string.pausing),
        )
    }

    override fun onTaskCancel(task: DownloadTask?) {

    }

    override fun onTaskFail(task: DownloadTask?, e: Exception?) {
        val entity = task?.entity ?: return
        val runtime = ariaId2Runtime[entity.id] ?: return
        synchronized(runtime.lock) {
            runtime.error(
                errorMsg = stringRes(com.heyanle.easy_i18n.R.string.download_error),
                error = e
            )
        }
    }

    override fun onTaskComplete(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val runtime = ariaId2Runtime[entity.id] ?: return
        synchronized(runtime.lock) {
            runtime.ariaDownloadFilePath = task.filePath
            runtime.m3u8Entity = task.entity.m3U8Entity
            runtime.stepCompletely(this)
        }

    }

    override fun onTaskRunning(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val runtime = ariaId2Runtime[entity.id] ?: return
        runtime.dispatchProcessToBus(
            task,
            stringRes(com.heyanle.easy_i18n.R.string.downloading),
        )
    }

    override fun onNoSupportBreakPoint(task: DownloadTask?) {
        stringRes(com.heyanle.easy_i18n.R.string.no_support_break_point).moeSnackBar()
    }

    private fun CartoonDownloadRuntime.dispatchProcessToBus(
        task: DownloadTask,
        status: String,
        // Null 则展示网速
        subStatus: String? = null,
    ) {

        val process = if (task.entity?.m3U8Entity != null) {
            // m3u8 无解
            -1f
        } else {
            if ((task.entity.fileSize) <= 0L) -1f else ((task.entity.percent) / 100f)
        }

        dispatchToBus(
            process,
            status,
            subStatus ?: if (task.entity.fileSize > 0L) task.convertSpeed?:"" else task.convertCurrentProgress ?:""
        )
    }
    private fun DownloadReceiver.getFirstTaskWithExt(
        ext: String
    ): DownloadEntity? {
        return DbEntity.findFirst<DownloadEntity>(
            DownloadEntity::class.java,
            "str=?",
            ext
        )
    }
}