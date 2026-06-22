package com.heyanle.easybangumi4.plugin.source.push

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.plugin.source.PluginV3
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import java.io.File

/**
 * Created by heyanlin on 2024/10/29.
 */
class PushFromCode(
    private val cacheFolder: String,
    private val sourceController: SourceController,
) : SourcePushTask {

    companion object {
        const val CACHE_FILE_NAME = "bangumi_extension_cache"
    }

    private val downloadRootFolder = cacheFolder


    override fun identify(): String {
        return SourcePushTask.SOURCE_PUSH_TASK_IDENTIFY_FROM_CODE
    }

    override suspend fun invoke(
        scope: CoroutineScope,
        param: SourcePushTask.Param,
        container: SourcePushController.SourcePushTaskContainer
    ) {
        scope.load(param, container)
    }

    private suspend fun CoroutineScope.load(
        param: SourcePushTask.Param,
        container: SourcePushController.SourcePushTaskContainer
    ) {
        val code = param.str1
        if (code.isEmpty()) {
            container.dispatchError(stringRes(R.string.is_empty))
        }
        container.dispatchLoadingMsg(stringRes(R.string.loading))
        try {
            val file = File(downloadRootFolder, "${CACHE_FILE_NAME}${PluginV3.JS_SOURCE_SUFFIX}")
            File(downloadRootFolder).mkdirs()
            file.delete()
            file.createNewFile()
            file.writeText(code)
            val res = sourceController.appendOrUpdateSource(file)
            yield()
            res.onOK {
                container.dispatchCompletely(stringRes(R.string.succeed))
            }.onError {
                container.dispatchError(it.throwable?.message?.toString() ?: it.errorMsg ?: stringRes(R.string.load_fail))
            }

        } catch (e: Throwable) {
            e.printStackTrace()
            container.dispatchError(e?.message?.toString() ?: stringRes(R.string.load_fail))
        }


    }
}

