package com.heyanle.easy_bangumi_cm.base


/**
 * TODO file logger
 * Created by HeYanLe on 2024/12/3 0:45.
 * https://github.com/heyanLE
 */

class DesktopLogger: Logger {

    override fun d(tag: String, msg: String) {
        println("[$tag] $msg")
    }

    override fun e(tag: String, msg: String, e: Throwable) {
        println("[$tag] $msg")
        e.printStackTrace()
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