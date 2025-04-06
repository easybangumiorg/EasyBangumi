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

expect interface Logger {
    fun isTraceEnabled(): Boolean
    fun trace(message: String?, throwable: Throwable?)

    fun isDebugEnabled(): Boolean
    fun debug(message: String?, throwable: Throwable?)

    fun isInfoEnabled(): Boolean
    fun info(message: String?, throwable: Throwable?)

    fun isWarnEnabled(): Boolean
    fun warn(message: String?, throwable: Throwable?)

    fun isErrorEnabled(): Boolean
    fun error(message: String?, throwable: Throwable?)
}

expect fun logger(tag: String): Logger

expect fun Any.logger(): Logger


enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
}
