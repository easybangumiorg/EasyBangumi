package org.easybangumi.next.lib.logger

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

interface DebugLoggerProxy {
    fun getLogger(tag: String): Logger
}
var debugLoggerProxy: DebugLoggerProxy? = null

expect interface Logger {

    fun trace(message: String?, throwable: Throwable? = null)
    fun debug(message: String?, throwable: Throwable? = null)
    fun info(message: String?, throwable: Throwable? = null)
    fun warn(message: String?, throwable: Throwable? = null)
    fun error(message: String?, throwable: Throwable? = null)

    fun isTraceEnabled(): Boolean
    fun isDebugEnabled(): Boolean
    fun isInfoEnabled(): Boolean
    fun isWarnEnabled(): Boolean
    fun isErrorEnabled(): Boolean

}

fun logger(tag: String, enable: Boolean) = if (enable) {logger(tag)} else object: Logger {
    override fun trace(message: String?, throwable: Throwable?) {

    }

    override fun debug(message: String?, throwable: Throwable?) {

    }

    override fun info(message: String?, throwable: Throwable?) {

    }

    override fun warn(message: String?, throwable: Throwable?) {

    }

    override fun error(message: String?, throwable: Throwable?) {

    }

    override fun isTraceEnabled(): Boolean {
        return false
    }

    override fun isDebugEnabled(): Boolean {
        return false
    }

    override fun isInfoEnabled(): Boolean {
        return false
    }

    override fun isWarnEnabled(): Boolean {
        return false
    }

    override fun isErrorEnabled(): Boolean {
        return false
    }
}

expect fun logger(tag: String): Logger

expect fun Any.logger(): Logger

