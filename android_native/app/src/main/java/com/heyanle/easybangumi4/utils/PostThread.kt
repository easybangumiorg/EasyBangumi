package com.heyanle.easybangumi4.utils

import android.os.Handler
import android.os.HandlerThread
import okhttp3.internal.notifyAll


/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class PostThread(
    private val tag: String = "PostThread ${System.currentTimeMillis()}",
) {

    private val prepareQueue = mutableListOf<Runnable>()
    private var handler: Handler? = null
    private val innerHandlerThread = InnerHandlerThread()

    inner class InnerHandlerThread: HandlerThread(tag) {
        override fun onLooperPrepared() {
            super.onLooperPrepared()
            this@PostThread.onLooperPrepared()
        }


    }

    init {
        innerHandlerThread.start()
    }

    fun post(runnable: Runnable){
        if (handler != null) {
            handler?.post(runnable)
            return
        }
        synchronized(prepareQueue){
            if(handler == null){
                prepareQueue.add(runnable)
            }else{
                handler?.post(runnable)
            }
        }
    }

    fun release() {
        innerHandlerThread.quitSafely()
    }

    private fun onLooperPrepared() {
        synchronized(prepareQueue) {
            val handler = Handler(innerHandlerThread.looper)
            prepareQueue.forEach {
                handler.post(it)
            }
            prepareQueue.clear()
            this.handler = handler
            prepareQueue.notifyAll()
        }
    }
}