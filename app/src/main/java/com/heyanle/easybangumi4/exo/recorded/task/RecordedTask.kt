package com.heyanle.easybangumi4.exo.recorded.task

import kotlinx.coroutines.flow.StateFlow
import java.lang.Exception

/**
 * Created by heyanlin on 2024/6/24.
 */
interface RecordedTask {

    data class TaskState(
        // 0 -> idle 1 -> doing 2 -> done 3 -> error
        val status: Int = 0,
        val statusLabel: String = "",
        val process: Int = -1,
        val errorException: Exception? = null,
        val error: String = ""
    )

    val state: StateFlow<TaskState>

    fun start()

    fun stop()

}