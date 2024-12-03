package com.heyanle.easy_bangumi_cm.shared.utils

import com.heyanle.easy_bangumi_cm.shared.base.CoroutineProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Created by heyanlin on 2024/12/3.
 */
class CoroutineProvider() {

    val io: CoroutineDispatcher = Dispatchers.IO

    val main: CoroutineDispatcher = Dispatchers.Main

    val single = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun newSingle(): CoroutineDispatcher {
        return Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }
}