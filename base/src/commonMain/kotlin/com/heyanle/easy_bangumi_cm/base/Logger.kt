package com.heyanle.easy_bangumi_cm.base


/**
 * Created by HeYanLe on 2024/12/3 0:15.
 * https://github.com/heyanLE
 */

interface Logger {

    fun d(tag: String, msg: String)

    fun e(tag: String, msg: String, e: Throwable)

    fun i(tag: String, msg: String)

    fun w(tag: String, msg: String)

    fun v(tag: String, msg: String)

    fun wtf(tag: String, msg: String)


}