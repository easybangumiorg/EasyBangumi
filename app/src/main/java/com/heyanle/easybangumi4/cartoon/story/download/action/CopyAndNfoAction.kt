package com.heyanle.easybangumi4.cartoon.story.download.action

import androidx.core.net.toUri
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.cartoon.story.local.LocalCartoonPreference
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jsoup.nodes.Element
import java.io.File

/**
 * Created by heyanle on 2024/8/4.
 * https://github.com/heyanLE
 */
class CopyAndNfoAction: BaseAction {

    companion object {
        const val NAME = "CopyAndNfoAction"
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
        val localPref: LocalCartoonPreference = Inject.get()
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

        val targetFolder = UniFile.fromUri(APP, localPref.realBangumiLocalUri.value)
            ?: throw IllegalStateException("target folder is null")

        val targetCartoonFolder = targetFolder.findFile(runtime.req.toLocalItemId)
            ?.let { if (it.isDirectory) it else null }
            ?: targetFolder.createDirectory(runtime.req.toLocalItemId)
            ?: throw IllegalStateException("target cartoon folder is null")
        val mediaNameP =
            "${runtime.req.toLocalItemId} ${runtime.req.toEpisodeTitle} S1E${runtime.req.toEpisode}"
        val mediaName = "${mediaNameP}.mp4"
        val nfoName = "${mediaNameP}.nfo"
        val tempSuffix = ".${runtime.req.uuid}.temp"
        targetCartoonFolder.findFile("$mediaName$tempSuffix")?.delete()
        targetCartoonFolder.findFile("$nfoName$tempSuffix")?.delete()
        val targetMediaFile = targetCartoonFolder.createFile("$mediaName$tempSuffix")
            ?: throw IllegalStateException("target media file is null")
        if (!targetMediaFile.canWrite()) {
            throw IllegalStateException("target media file can not write")
        }
        sourceFile.inputStream().buffered().use { inp ->
            targetMediaFile.openOutputStream().buffered().use { outp ->
                inp.copyTo(outp)
                outp.flush()
            }
        }

        // 先完整写入两个临时文件，再发布正式名称，避免复制/NFO 失败时暴露半成品。
        val nfoFile = targetCartoonFolder.createFile("$nfoName$tempSuffix")
        if (nfoFile == null || !nfoFile.canWrite()) {
            targetMediaFile.delete()
            throw IllegalStateException("nfo file is null or can not write")
        }

        val details = Element("episodedetails")
        details.appendElement("title").text(runtime.req.toEpisodeTitle)
        details.appendElement("season").text("1")
        details.appendElement("episode").text(runtime.req.toEpisode.toString())

        val text = details.outerHtml()
        try {
            nfoFile.openOutputStream().bufferedWriter().use {
                it.write(text)
            }
            targetCartoonFolder.findFile(nfoName)?.delete()
            targetCartoonFolder.findFile(mediaName)?.delete()
            if (!nfoFile.renameTo(nfoName)) {
                throw IllegalStateException("publish nfo file failed")
            }
            if (!targetMediaFile.renameTo(mediaName)) {
                targetCartoonFolder.findFile(nfoName)?.delete()
                throw IllegalStateException("publish media file failed")
            }
        } catch (error: Throwable) {
            targetMediaFile.delete()
            nfoFile.delete()
            throw error
        }

        sourceFile.delete()
        runtime.stepCompletely(this)
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {

    }
}
