package com.heyanle.easy_bangumi_cm.base

import android.util.Log


/**
 * Created by HeYanLe on 2024/12/3 0:23.
 * https://github.com/heyanLE
 */

class AndroidLogger: Logger {

    init {
        logger = this
    }

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