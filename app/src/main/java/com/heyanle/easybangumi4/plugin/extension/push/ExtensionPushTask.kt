package com.heyanle.easybangumi4.plugin.extension.push

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Created by heyanlin on 2024/10/29.
 */
interface ExtensionPushTask {

    companion object {
        const val EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_URL = "from_file_url"
        const val EXTENSION_PUSH_TASK_IDENTIFY_FROM_CODE = "from_code"
        const val EXTENSION_PUSH_TASK_IDENTIFY_FROM_FILE_REPO = "from_file_repo"
    }

    data class Param(
        val identify: String,
        val str1: String,
        val str2: String,
    )

    fun identify(): String

    suspend fun CoroutineScope.invoke(
        param: Param,
        container: ExtensionPushController.ExtensionPushTaskContainer,
    )

}