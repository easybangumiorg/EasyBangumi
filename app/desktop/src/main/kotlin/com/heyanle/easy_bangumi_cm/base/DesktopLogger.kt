package com.heyanle.easy_bangumi_cm.base

import com.heyanle.easy_bangumi_cm.base.Logger
import com.heyanle.easy_bangumi_cm.base.logger

/**
 * TODO file logger
 * Created by HeYanLe on 2024/12/3 0:45.
 * https://github.com/heyanLE
 */

class DesktopLogger: Logger{

    init {
        logger = this
    }

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