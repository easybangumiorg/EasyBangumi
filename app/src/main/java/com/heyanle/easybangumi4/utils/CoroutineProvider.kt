package com.heyanle.easybangumi4.utils

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object CoroutineProvider {

    val SINGLE = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    val CUSTOM_SINGLE get() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

}