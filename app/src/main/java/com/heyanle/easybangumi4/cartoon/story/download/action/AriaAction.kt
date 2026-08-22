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
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadArtifact
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngine
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineContext
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineDescriptor
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineIds
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadMediaType
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadToggleResult
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by heyanle on 2024/8/3.
 * https://github.com/heyanLE
 */
class AriaAction(
    application: Application,
    downloadPreference: CartoonDownloadPreference
) : QuickDownloadEngine, DownloadTaskListener {

    companion object {
        const val NAME = "AriaAction"
    }

    override val descriptor = QuickDownloadEngineDescriptor(
        id = QuickDownloadEngineIds.ARIA,
        displayName = "Aria",
        supportedMediaTypes = setOf(
            QuickDownloadMediaType.DIRECT,
            QuickDownloadMediaType.HLS,
        ),
    )

    private val aria: DownloadReceiver by lazy {
        Aria.download(this@AriaAction).apply {
            register()
        }
    }

    private val ariaId2Context = ConcurrentHashMap<Long, QuickDownloadEngineContext>()
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
            val base = URI(m3u8Url)
            tsUrls.map { base.resolve(it).toString() }
        }
        setBandWidthUrlConverter { m3u8Url, bandWidthUrl ->
            URI(m3u8Url).resolve(bandWidthUrl).toString()
        }
        setUseDefConvert(false)
        generateIndexFile()
    }

    // action
    override suspend fun canResume(request: CartoonDownloadReq): Boolean {
        return withContext(Dispatchers.IO) {
            val task = aria.getFirstTaskWithExt(request.uuid) ?: return@withContext false
            if (task.isComplete) {
                return@withContext true
            }
            (task.state == DownloadEntity.STATE_WAIT ||
                    task.state == DownloadEntity.STATE_COMPLETE ||
                    task.state == DownloadEntity.STATE_POST_PRE ||
                    task.state == DownloadEntity.STATE_RUNNING ||
                    task.state == DownloadEntity.STATE_STOP)
        }
    }


    override suspend fun toggle(taskId: String): QuickDownloadToggleResult {
        val entity = aria.getFirstTaskWithExt(taskId)
            ?: return QuickDownloadToggleResult.UNSUPPORTED
        when(entity.state){
            IEntity.STATE_RUNNING, IEntity.STATE_WAIT -> {
                aria.load(entity.id).ignoreCheckPermissions().stop()
                return QuickDownloadToggleResult.PAUSED
            }
            IEntity.STATE_STOP -> {
                aria.load(entity.id).ignoreCheckPermissions().resume()
                return QuickDownloadToggleResult.RESUMED
            }
            else -> return QuickDownloadToggleResult.UNSUPPORTED
        }
    }

    override fun start(context: QuickDownloadEngineContext) {
        "push aria action".logi("Action")
        val entity = aria.getFirstTaskWithExt(context.taskId)

        File(downloadFolder).mkdirs()
        if (entity != null) {
            ariaId2Context[entity.id] = context
            if (entity.state == IEntity.STATE_STOP ||
                entity.state == IEntity.STATE_WAIT ||
                entity.state == IEntity.STATE_RUNNING ||
                entity.state == IEntity.STATE_POST_PRE
            ) {
                aria.load(entity.id).ignoreCheckPermissions().resume()
            } else if (entity.state == IEntity.STATE_COMPLETE) {
                context.complete(entity.toArtifact())
            } else {
                aria.load(entity.id).cancel(true)
                ariaId2Context.remove(entity.id)
                context.fail(null, "已清理无效的 Aria 检查点，请重试")
            }
        } else {
            createTask(context)
        }
    }

    private fun createTask(context: QuickDownloadEngineContext) {
        val playerInfo = context.playerInfo
        val taskId = when (playerInfo.decodeType) {
            PlayerInfo.DECODE_TYPE_OTHER -> {
                val path = File(downloadFolder, context.taskId + ".mp4").absolutePath
                aria.load(playerInfo.uri)
                    .setExtendField(context.taskId)
                    .option(playerInfo.toHttpOption())
                    .setFilePath(path)
                    .ignoreCheckPermissions()
                    .ignoreFilePathOccupy()
                    .create()
            }

            PlayerInfo.DECODE_TYPE_HLS -> {
                val path = File(downloadFolder, context.taskId).absolutePath
                aria.load(playerInfo.uri)
                    .setExtendField(context.taskId)
                    .option(playerInfo.toHttpOption())
                    .setFilePath(path)
                    .m3u8VodOption(m3u8Option)
                    .ignoreFilePathOccupy()
                    .ignoreCheckPermissions()
                    .create()
            }

            else -> -1L
        }
        if (taskId == -1L) {
            context.fail(null, "创建 Aria 下载任务失败")
        } else {
            ariaId2Context[taskId] = context
        }
    }

    private fun PlayerInfo.toHttpOption() = HttpOption().apply {
        header?.forEach { addHeader(it.key, it.value) }
    }

    override fun cancel(taskId: String) {
        val entity = aria.getFirstTaskWithExt(taskId) ?: return
        ariaId2Context.remove(entity.id)
        aria.load(entity.id)?.cancel(true)
    }

    override fun clear(taskId: String) {
        val ids = ariaId2Context.entries
            .filter { it.value.taskId == taskId }
            .map { it.key }
        ids.forEach(ariaId2Context::remove)
    }

    // aria callback
    override fun onWait(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val context = ariaId2Context[entity.id] ?: return
        context.dispatchProcess(
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
        val context = ariaId2Context[entity.id] ?: return
        context.dispatchProcess(
            task,
            stringRes(com.heyanle.easy_i18n.R.string.pausing),
        )
    }

    override fun onTaskCancel(task: DownloadTask?) {

    }

    override fun onTaskFail(task: DownloadTask?, e: Exception?) {
        val entity = task?.entity ?: return
        val context = ariaId2Context.remove(entity.id) ?: return
        context.fail(e, stringRes(com.heyanle.easy_i18n.R.string.download_error))
    }

    override fun onTaskComplete(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val context = ariaId2Context.remove(entity.id) ?: return
        context.complete(task.entity.toArtifact())

    }

    override fun onTaskRunning(task: DownloadTask?) {
        val entity = task?.entity ?: return
        val context = ariaId2Context[entity.id] ?: return
        context.dispatchProcess(
            task,
            stringRes(com.heyanle.easy_i18n.R.string.downloading),
        )
    }

    override fun onNoSupportBreakPoint(task: DownloadTask?) {
        stringRes(com.heyanle.easy_i18n.R.string.no_support_break_point).moeSnackBar()
    }

    private fun QuickDownloadEngineContext.dispatchProcess(
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

        report(
            process,
            status,
            subStatus ?: if (task.entity.fileSize > 0L) task.convertSpeed?:"" else task.convertCurrentProgress ?:""
        )
    }

    private fun DownloadEntity.toArtifact(): QuickDownloadArtifact {
        val hls = m3U8Entity
        return if (hls == null) {
            QuickDownloadArtifact.DirectFile(filePath)
        } else {
            QuickDownloadArtifact.HlsBundle(
                filePath = hls.filePath,
                keyPath = hls.keyPath,
                method = hls.method,
                iv = hls.iv,
            )
        }
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
