package com.heyanle.easy_bangumi_cm.shared.platform

import com.heyanle.easy_bangumi_cm.shared.model.system.ILogger

actual class PlatformLogger : ILogger {
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