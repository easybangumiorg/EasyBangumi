package com.heyanle.easybangumi4.plugin.extension.push

import android.content.Context
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import kotlinx.coroutines.CoroutineScope

/**
 * Created by heyanlin on 2024/10/30.
 */
class PushFromRepo(
    private val context: Context,
    private val extensionController: ExtensionController,
) : ExtensionPushTask {

    override fun identify(): String {
        return ExtensionPushTask.EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_REPO
    }

    override suspend fun CoroutineScope.invoke(
        param: ExtensionPushTask.Param,
        container: ExtensionPushController.ExtensionPushTaskContainer
    ) {
        TODO("Not yet implemented")
    }
}