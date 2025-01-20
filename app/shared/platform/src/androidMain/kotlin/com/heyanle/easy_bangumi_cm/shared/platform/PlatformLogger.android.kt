package com.heyanle.easy_bangumi_cm.shared.platform

import android.util.Log
import com.heyanle.easy_bangumi_cm.shared.model.system.ILogger

actual class PlatformLogger : ILogger {

    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun e(tag: String, msg: String, e: Throwable?) {
        Log.e(tag, msg, e)
    }

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    override fun wtf(tag: String, msg: String) {
        Log.wtf(tag, msg)
    }
}