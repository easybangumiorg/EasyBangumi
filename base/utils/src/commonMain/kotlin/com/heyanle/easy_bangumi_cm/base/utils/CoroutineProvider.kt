package com.heyanle.easy_bangumi_cm.base.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by heyanlin on 2024/12/3.
 */
object CoroutineProvider {

    val io: CoroutineDispatcher = Dispatchers.IO

    val main: CoroutineDispatcher = Dispatchers.Main

    val single: CoroutineDispatcher by lazy {
        newSingle()
    }

    fun newSingle(): CoroutineDispatcher {
        return ThreadPoolExecutor(
            0, 1,
            10L, TimeUnit.SECONDS,
            LinkedBlockingQueue()
        ).asCoroutineDispatcher()
    }
}