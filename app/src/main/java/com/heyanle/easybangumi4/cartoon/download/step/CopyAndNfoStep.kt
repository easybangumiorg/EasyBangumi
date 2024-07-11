package com.heyanle.easybangumi4.cartoon.download.step

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadRuntimeFactory
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.cartoon.local.LocalCartoonPreference
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import org.jsoup.nodes.Element

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
object CopyAndNfoStep: BaseStep {

    const val NAME = "copy_and_nfo"

    override fun invoke() {
        val runtime = CartoonDownloadRuntimeFactory.runtimeLocal.get()
            ?: throw IllegalStateException("runtime is null")
        val localPref: LocalCartoonPreference = Inject.get()
        runtime.state = 1
        runtime.dispatchToBus(
            -1f,
            stringRes(com.heyanle.easy_i18n.R.string.copying),
        )
        runtime.canCancel = false

        val cacheFolder = UniFile.fromUri(APP, runtime.cacheFolderUri?.toUri()) ?: throw IllegalStateException("cache folder is null")
        val cacheTarget = cacheFolder.createFile(runtime.cacheDisplayName) ?: throw IllegalStateException("cache file is null")
        if (!cacheTarget.exists() || !cacheTarget.canRead()){
            throw IllegalStateException("cache file is not exists or can not read")
        }
        val targetFolder = UniFile.fromUri(APP, localPref.realLocalUri.value) ?: throw IllegalStateException("target folder is null")
        val targetCartoonFolder = targetFolder.createDirectory(runtime.req.toLocalItemId) ?: throw IllegalStateException("target cartoon folder is null")
        val mediaNameP = "${runtime.req.toLocalItemId} ${runtime.req.toEpisodeTitle} S1E${runtime.req.toEpisode}"
        val mediaName = "${mediaNameP}.mp4"
        val targetMediaFile = targetCartoonFolder.createFile("$mediaName.temp") ?: throw IllegalStateException("target media file is null")
        if (!targetMediaFile.canWrite()) {
            throw IllegalStateException("target media file can not write")
        }
        cacheTarget.openInputStream().use {  inp ->
            targetMediaFile.openOutputStream().use  { outp ->
                inp.copyTo(outp)
            }
        }
        targetMediaFile.renameTo(mediaName)

        // write nfo
        val nfoFile = targetCartoonFolder.createFile("${mediaNameP}.nfo")
        if (nfoFile == null || !nfoFile.canWrite()){
            runtime.stepCompletely()
            return
        }

        val details = Element("episodedetails")
        details.appendElement("title").text(runtime.req.toEpisodeTitle)
        details.appendElement("season").text("1")
        details.appendElement("episode").text(runtime.req.toEpisode.toString())

        val text = details.outerHtml()
        nfoFile.openOutputStream().bufferedWriter().use {
            it.write(text)
        }

        runtime.stepCompletely()



    }

    override fun cancel(runtime: CartoonDownloadRuntime) {

    }
}