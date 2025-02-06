package com.heyanle.easy_bangumi_cm.base.service.system
/**
 * ILogger is an interface that defines a set of logging methods for different log levels.
 * Implementations of this interface should provide the actual logging mechanism for each log level.
 */
interface ILogger {
    fun d(tag: String, msg: String)

    fun e(tag: String, msg: String, e: Throwable?)

    fun i(tag: String, msg: String)

    fun w(tag: String, msg: String)

    fun v(tag: String, msg: String)

    fun wtf(tag: String, msg: String)
}