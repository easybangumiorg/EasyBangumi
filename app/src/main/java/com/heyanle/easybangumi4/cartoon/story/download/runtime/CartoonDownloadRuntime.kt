package com.heyanle.easybangumi4.cartoon.story.download.runtime

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq

/**
 * Created by heyanle on 2024/8/2.
 * https://github.com/heyanLE
 */
class CartoonDownloadRuntime(
    val req: CartoonDownloadReq,
) {

    interface Listener {
        fun onStateChange(runtime: CartoonDownloadRuntime)
    }
    var listener: Listener? = null

    enum class State {
        WAITING, DOING, STEP_COMPLETELY, ERROR, SUCCESS, CANCEL
    }

    @Volatile
    var state: State = State.WAITING
        set(value) {
            field = value
            listener?.onStateChange(this)
        }

    val lock = Object()

    val dispatcherRunnable: Runnable? = null

}