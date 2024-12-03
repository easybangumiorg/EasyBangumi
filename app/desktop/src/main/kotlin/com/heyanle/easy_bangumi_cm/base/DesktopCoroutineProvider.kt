package com.heyanle.easy_bangumi_cm.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Created by heyanlin on 2024/12/3.
 */
class DesktopCoroutineProvider() : CoroutineProvider {

    override val io: CoroutineDispatcher
        get() = Dispatchers.IO

    override val main: CoroutineDispatcher
        get() = Dispatchers.Main

    private val singleDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    override val single: CoroutineDispatcher
        get() = singleDispatcher

    override fun newSingle(): CoroutineDispatcher {
        return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }
}