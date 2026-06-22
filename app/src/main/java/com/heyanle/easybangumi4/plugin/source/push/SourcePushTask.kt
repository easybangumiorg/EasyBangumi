package com.heyanle.easybangumi4.plugin.source.push

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Created by heyanlin on 2024/10/29.
 */
interface SourcePushTask {

    companion object {
        const val SOURCE_PUSH_TASK_IDENTIFY_FROM_FILE_URL = "from_file_url"
        const val SOURCE_PUSH_TASK_IDENTIFY_FROM_CODE = "from_code"
        const val SOURCE_PUSH_TASK_IDENTIFY_FROM_FILE_REPO = "from_file_repo"
    }

    data class Param(
        val identify: String,
        val str1: String = "",
        val str2: String = "",
    )

    fun identify(): String

    suspend fun invoke(
        scope: CoroutineScope,
        param: Param,
        container: SourcePushController.SourcePushTaskContainer,
    )

}

