package com.heyanle.easybangumi4.cartoon.story.download.action

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.bound.BoundMediaCase
import com.heyanle.easybangumi4.cartoon.story.bound.FlatDownloadController
import com.heyanle.easybangumi4.cartoon.story.bound.FlatDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.bound.FlatFileNameSanitizer
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.utils.stringRes
import com.hippo.unifile.UniFile
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * 落盘到扁平下载目录：无元数据（不写 NFO），文件名默认“番名-集名”，
 * 成功后自动建立该集到文件的绑定并刷新目录索引。
 */
class CopyToFlatAction(
    private val flatDownloadPreference: FlatDownloadPreference,
    private val flatDownloadController: FlatDownloadController,
    private val boundMediaCase: BoundMediaCase,
) : BaseAction {

    companion object {
        const val NAME = "CopyToFlatAction"
    }

    private val mainScope = MainScope()

    override fun isAsyncAction(): Boolean {
        return false
    }

    override suspend fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean {
        return false
    }

    override suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        return false
    }

    override fun push(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        val runtime = cartoonDownloadRuntime
        mainScope.launch {
            cartoonDownloadRuntime.dispatchToBus(
                -1f,
                stringRes(R.string.copying),
            )
        }

        val sourcePath = runtime.filePathBeforeCopy
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists() || !sourceFile.canRead()) {
            throw IllegalStateException("source file is not exists or can not read")
        }

        val targetFolder = UniFile.fromUri(APP, flatDownloadPreference.realFlatDownloadUri.value)
            ?: throw IllegalStateException("flat download folder is null")

        val baseName = FlatFileNameSanitizer.sanitize(
            runtime.req.flatFileName?.takeIf { it.isNotBlank() }
                ?: FlatFileNameSanitizer.defaultName(
                    runtime.req.fromCartoonInfo.name,
                    runtime.req.toEpisodeTitle,
                )
        )

        // 目录即索引：以实际目录内容做查重，冲突时追加集数序号
        var candidate = baseName
        if (existsInFlatFolder(targetFolder, candidate)) {
            candidate = FlatFileNameSanitizer.sanitize("$baseName-${runtime.req.fromEpisode.order}")
        }
        var attempt = 2
        while (existsInFlatFolder(targetFolder, candidate)) {
            candidate = FlatFileNameSanitizer.sanitize("$baseName-${runtime.req.fromEpisode.order}-$attempt")
            attempt++
        }

        val mediaName = "$candidate.mp4"
        val tempSuffix = ".${runtime.req.uuid}.temp"
        val targetMediaFile = targetFolder.createFile("$mediaName$tempSuffix")
            ?: throw IllegalStateException("flat media file is null")
        if (!targetMediaFile.canWrite()) {
            throw IllegalStateException("flat media file can not write")
        }

        try {
            sourceFile.inputStream().buffered().use { inp ->
                targetMediaFile.openOutputStream().buffered().use { outp ->
                    inp.copyTo(outp)
                    outp.flush()
                }
            }
            if (!targetMediaFile.renameTo(mediaName)) {
                throw IllegalStateException("publish flat media file failed")
            }
        } catch (error: Throwable) {
            targetMediaFile.delete()
            throw error
        }

        // 下载即绑定：以实际落盘的文件名建立集级绑定
        boundMediaCase.bindDownloadedFlatFile(
            runtime.req.fromCartoonInfo,
            runtime.req.fromPlayLine,
            runtime.req.fromEpisode,
            candidate,
        )
        flatDownloadController.refresh()

        sourceFile.delete()
        runtime.stepCompletely(this)
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {

    }

    private fun existsInFlatFolder(folder: UniFile, name: String): Boolean {
        return folder.findFile("$name.mp4") != null || folder.findFile("$name.mkv") != null
    }
}
