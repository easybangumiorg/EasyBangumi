package com.heyanle.easybangumi4.plugin.extension.push

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.utils.downloadTo
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import org.json.JSONObject
import java.io.File

/**
 * Created by heyanlin on 2024/10/30.
 */
class PushFromRepo(
    private val cacheFolder: String,
    private val extensionController: ExtensionController,
) : ExtensionPushTask {

    companion object {
        const val CACHE_REPO_JSONL_NAME = "extension_repo.jsonl"
    }

    override fun identify(): String {
        return ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_REPO
    }

    override suspend fun invoke(
        scope: CoroutineScope,
        param: ExtensionPushTask.Param,
        container: ExtensionPushController.ExtensionPushTaskContainer
    ) {
        scope.load(param, container)
    }

    private suspend fun CoroutineScope.load(
        param: ExtensionPushTask.Param,
        container: ExtensionPushController.ExtensionPushTaskContainer
    ) {
        if (param.str1.isEmpty()) {
            container.dispatchError(stringRes(R.string.is_empty))
            return
        }
        container.dispatchLoadingMsg(stringRes(R.string.loading))

        val repoJsonlFile = File(cacheFolder, CACHE_REPO_JSONL_NAME)
        repoJsonlFile.delete()
        // 1. 下载 jsonl
        param.str1.downloadTo(repoJsonlFile.absolutePath)
        if (!repoJsonlFile.exists() || repoJsonlFile.length().toInt() == 0){
            container.dispatchError(stringRes(R.string.load_fail))
            return
        }

        // 2. 解析 jsonl
        val taskList = repoJsonlFile.bufferedReader().use {
            it.lineSequence().map {
                JSONObject(it)
            }.map {
                val url = it.optString("url")
                val key = it.optString("key")
                if (url.isEmpty() || key.isEmpty()) {
                    return@map null
                }
                key to url
            }
        }.filterIsInstance<Pair<String, String>>().map {
            File(cacheFolder, it.first) to it.second
        }.toList()

        val allCount = taskList.size
        var completelyDownloadCount: Int = 0
        var completelyLoadCount: Int = 0

        taskList.map {
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
                File(cacheFolder, it.first.name + ".${JsExtensionProvider.EXTENSION_CRY_SUFFIX}")
            } else {
                File(cacheFolder, it.first.name + ".${JsExtensionProvider.EXTENSION_SUFFIX}")
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