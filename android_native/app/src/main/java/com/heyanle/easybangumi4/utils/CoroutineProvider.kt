package com.heyanle.easybangumi4.utils

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object CoroutineProvider {

    val SINGLE = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val newSingleExecutor
        get() = ThreadPoolExecutor(
            0, 1,
            10L, TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )

    // 未使用的线程将在 10 秒后被终止
    val newSingleDispatcher
        get() = ThreadPoolExecutor(
            0, 1,
            10L, TimeUnit.SECONDS,
            LinkedBlockingQueue()
        ).asCoroutineDispatcher()

}