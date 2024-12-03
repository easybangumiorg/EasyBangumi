package com.heyanle.easy_bangumi_cm.base

import org.koin.core.logger.Level
import org.koin.core.logger.MESSAGE

typealias KoinLogger = org.koin.core.logger.Logger

/**
 * TODO file logger
 * Created by HeYanLe on 2024/12/3 0:45.
 * https://github.com/heyanLE
 */

class DesktopLogger: Logger, KoinLogger(){

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

    override fun display(level: Level, msg: MESSAGE) {
        when(level){
            Level.DEBUG -> d("Koin", msg)
            Level.INFO -> i("Koin", msg)
            Level.ERROR -> e("Koin", msg, null)
            Level.NONE -> {}
            else -> {}
        }
    }
}