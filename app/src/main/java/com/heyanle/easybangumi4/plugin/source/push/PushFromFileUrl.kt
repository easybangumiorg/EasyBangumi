package com.heyanle.easybangumi4.plugin.source.push

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.plugin.source.PluginV3
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.utils.downloadTo
import com.heyanle.easybangumi4.utils.getMD5
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import java.io.File

class PushFromFileUrl(
    private val cacheFolder: String,
    private val sourceController: SourceController,
) : SourcePushTask {

    override fun identify(): String {
        return SourcePushTask.SOURCE_PUSH_TASK_IDENTIFY_FROM_FILE_URL
    }

    override suspend fun invoke(
        scope: CoroutineScope,
        param: SourcePushTask.Param,
        container: SourcePushController.SourcePushTaskContainer,
    ) {
        scope.load(param, container)
    }

    private suspend fun CoroutineScope.load(
        param: SourcePushTask.Param,
        container: SourcePushController.SourcePushTaskContainer,
    ) {
        if (param.str1.isEmpty()) {
            container.dispatchError(stringRes(R.string.is_empty))
            return
        }
        container.dispatchLoadingMsg(stringRes(R.string.loading))

        val rootFile = File(cacheFolder)
        rootFile.deleteRecursively()
        rootFile.mkdirs()

        val urls = param.str1.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var completelyDownloadCount = 0
        var completelyLoadCount = 0

        urls.map { url ->
            val downloaded = File(rootFile, url.getMD5())
            try {
                downloaded.delete()
                runCatching {
                    url.downloadTo(downloaded.absolutePath)
                }.onFailure {
                    it.printStackTrace()
                }
                yield()
                if (downloaded.exists() && downloaded.length() > 0) {
                    completelyDownloadCount++
                    container.dispatchLoadingMsg(
                        stringRes(R.string.downloading) + "${completelyDownloadCount}/${urls.size}"
                    )
                    downloaded
                } else {
                    null
                }
            } catch (e: Throwable) {
                null
            }
        }.filterIsInstance<File>().forEach { downloaded ->
            val sourceFile = File(rootFile, downloaded.name + PluginV3.JS_SOURCE_SUFFIX)
            downloaded.renameTo(sourceFile)
            yield()
            if (sourceController.appendOrUpdateSource(sourceFile) is DataResult.Ok) {
                completelyLoadCount++
                container.dispatchLoadingMsg(
                    stringRes(R.string.loading) + "${completelyLoadCount}/${completelyDownloadCount}"
                )
            }
        }

        val msg = "${stringRes(R.string.succeed)} ${completelyLoadCount}\n" +
            "${stringRes(R.string.download_fail)} ${urls.size - completelyDownloadCount}\n" +
            "${stringRes(R.string.load_fail)} ${completelyDownloadCount - completelyLoadCount}"
        yield()
        if (completelyLoadCount == 0) {
            container.dispatchError(msg)
        } else {
            container.dispatchCompletely(msg)
        }
    }
}

