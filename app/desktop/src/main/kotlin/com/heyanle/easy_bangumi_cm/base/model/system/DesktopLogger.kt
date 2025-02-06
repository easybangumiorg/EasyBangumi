package com.heyanle.easy_bangumi_cm.base.model.system

import com.heyanle.easy_bangumi_cm.base.service.system.ILogger


/**
 * Created by HeYanLe on 2025/2/4 17:50.
 * https://github.com/heyanLE
 */

class DesktopLogger: ILogger {
    override fun d(tag: String, msg: String) {
        println("[$tag] $msg")
    }

    override fun e(tag: String, msg: String, e: Throwable?) {
        println("[$tag] $msg")
        e?.printStackTrace()
    }

    override fun i(tag: String, msg: String) {
        println("[$tag] $msg")
    }

    override fun w(tag: String, msg: String) {
        println("[$tag] $msg")
    }

    override fun v(tag: String, msg: String) {
        println("[$tag] $msg")
    }

    override fun wtf(tag: String, msg: String) {
        println("[$tag] $msg")
    }
}