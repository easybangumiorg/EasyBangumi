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

expect fun logger(tag: String): Logger

expect fun Any.logger(): Logger

