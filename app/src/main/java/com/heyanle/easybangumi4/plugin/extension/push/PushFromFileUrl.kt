package com.heyanle.easybangumi4.plugin.extension.push

import android.content.Context
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.utils.downloadTo
import com.heyanle.easybangumi4.utils.getMD5
import kotlinx.coroutines.yield
import java.io.File

/**
 * 从 url 下载，一行一个
 * Created by heyanlin on 2024/10/29.
 */
class PushFromFileUrl(
    private val context: Context,
    private val extensionController: ExtensionController,
) : ExtensionPushTask {

    private val downloadRootFolder = context.getCachePath("js_file_download")

    override fun identify(): String {
        return ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_URL
    }

    override suspend fun CoroutineScope.invoke(
        param: ExtensionPushTask.Param,
        container: ExtensionPushController.ExtensionPushTaskContainer
    ) {
        if (param.str1.isEmpty()) {
            container.dispatchError(stringRes(R.string.is_empty))
            return
        }
        container.dispatchLoadingMsg(stringRes(R.string.loading))

        val rootFile = File(downloadRootFolder)
        rootFile.delete()
        rootFile.mkdirs()

        val urlArray = param.str1.split("\n")
        val allCount = urlArray.size
        var completelyDownloadCount: Int = 0
        var completelyLoadCount: Int = 0
        urlArray.mapIndexed { index, url ->
            // 按行解析
            File(rootFile, url.getMD5()) to url
        }.map {
            // 下载
            try {
                it.first.delete()
                it.second.downloadTo(it.first.absolutePath)
                yield()
                if (it.first.exists() && it.first.length() > 0){
                    completelyDownloadCount ++
                    container.dispatchLoadingMsg(stringRes( R.string.downloading) + "${completelyDownloadCount}/${allCount}")
                    it.first
                } else {
                    null
                }
            } catch (e: Throwable) {
                null
            }
        }.filterIsInstance<File>().map {
            // 根据首行判断是否是加密 jsc
            val firstLine = it.bufferedReader().use { it.readLine() }
            if (firstLine == JSExtensionCryLoader.FIRST_LINE_MARK) {
                it to true
            } else {
                it to false
            }
        }.map {
            // 添加后缀
            val targetFile = if (it.second) {
                File(downloadRootFolder, it.first.name + ".${JsExtensionProvider.EXTENSION_CRY_SUFFIX}")
            } else {
                File(downloadRootFolder, it.first.name + ".${JsExtensionProvider.EXTENSION_SUFFIX}")
            }
            it.first.renameTo(targetFile)
            yield()
            targetFile
        }.let {
            // 添加完再扫描
            extensionController.withNoWatching(true) {
                yield()
                it.map {
                    // 加载
                    val e = extensionController.appendExtensionFile(it)
                    yield()
                    if (e == null) {
                        completelyLoadCount ++
                        container.dispatchLoadingMsg(stringRes( R.string.loading) + "${completelyLoadCount}/${completelyDownloadCount}")
                    }
                    e
                }
            }
        }
        val msg = "${stringRes(R.string.succeed)} ${completelyLoadCount}\n" +
                "${stringRes(R.string.download_fail)} ${allCount - completelyDownloadCount}\n" +
                "${stringRes(R.string.load_fail)} ${completelyDownloadCount - completelyLoadCount}"
        yield()
        if (completelyLoadCount == 0) {
            container.dispatchError(msg)
        } else {
            container.dispatchCompletely(msg)
        }


    }
}