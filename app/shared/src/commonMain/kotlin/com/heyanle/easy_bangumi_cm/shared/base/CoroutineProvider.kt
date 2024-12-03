package com.heyanle.easy_bangumi_cm.shared.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Created by heyanlin on 2024/12/3.
 */
interface CoroutineProvider {

    val io: CoroutineDispatcher
        get() = Dispatchers.IO

    val single: CoroutineDispatcher

    val main: CoroutineDispatcher

    fun newSingle(): CoroutineDispatcher

}