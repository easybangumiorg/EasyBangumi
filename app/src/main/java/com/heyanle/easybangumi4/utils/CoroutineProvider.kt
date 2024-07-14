package com.heyanle.easybangumi4.utils

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object CoroutineProvider {

    val SINGLE = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val CUSTOM_SINGLE get() =  Executors.newSingleThreadExecutor().asCoroutineDispatcher()

}