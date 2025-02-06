package com.heyanle.easy_bangumi_cm.base.model.system

import android.util.Log
import com.heyanle.easy_bangumi_cm.base.service.system.ILogger


/**
 * Created by HeYanLe on 2025/2/4 17:42.
 * https://github.com/heyanLE
 */

class AndroidLogger: ILogger {

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