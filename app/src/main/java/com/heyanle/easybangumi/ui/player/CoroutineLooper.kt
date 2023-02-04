package com.heyanle.easybangumi.ui.player

import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

/**
 * Created by HeYanLe on 2023/1/29 22:14.
 * https://github.com/heyanLE
 */
class CoroutineLooper(
    val run: suspend () -> Unit,
    val delay: Long = 2000
) {

    val loopScope: CoroutineScope? = null

    @UiThread
    fun start() {

    }

    @UiThread
    fun stop() {
    }

    private suspend fun loop() {
        while (loopScope?.isActive == true) {

        }
    }
}