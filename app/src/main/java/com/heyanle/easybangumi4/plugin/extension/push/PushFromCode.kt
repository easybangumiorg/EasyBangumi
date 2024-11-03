package com.heyanle.easybangumi4.plugin.extension.push

import android.content.Context
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.utils.getCachePath
import kotlinx.coroutines.yield
import java.io.File

/**
 * Created by heyanlin on 2024/10/29.
 */
class PushFromCode(
    private val cacheFolder: String,
    private val extensionController: ExtensionController,
) : ExtensionPushTask {

    companion object {
        const val CACHE_FILE_NAME = "bangumi_extension_cache"
    }

    private val downloadRootFolder = cacheFolder


    override fun identify(): String {
        return ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_CODE
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
        val code = param.str1
        if (code.isEmpty()) {
            container.dispatchError(stringRes(R.string.is_empty))
        }
        container.dispatchLoadingMsg(stringRes(R.string.loading))
        try {
            val firstLine = code.lineSequence().firstOrNull()
            val isCry = firstLine == JSExtensionCryLoader.FIRST_LINE_MARK
            val suffix = if (isCry) JsExtensionProvider.EXTENSION_CRY_SUFFIX else JsExtensionProvider.EXTENSION_SUFFIX
            val file = File(downloadRootFolder, "${CACHE_FILE_NAME}.${suffix}")
            File(downloadRootFolder).mkdirs()
            file.delete()
            file.createNewFile()
            file.writeText(code)
            val e = extensionController.appendExtensionFile(file)
            yield()
            if (e == null) {
                container.dispatchCompletely(stringRes(R.string.succeed))
            } else {
                e.printStackTrace()
                container.dispatchError(e?.message?.toString() ?: stringRes(R.string.load_fail))
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            container.dispatchError(e?.message?.toString() ?: stringRes(R.string.load_fail))
        }


    }
}