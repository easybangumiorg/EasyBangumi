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
class KotlinLoggerProxy: DebugLoggerProxy {
    override fun getLogger(tag: String): Logger {
        return KotlinLogger(tag)
    }
}
class KotlinLogger(private val tag: String): Logger {

    override fun trace(message: String?, throwable: Throwable?) {
        println("TRACE:[$tag] $message")
    }

    override fun debug(message: String?, throwable: Throwable?) {
        println("DEBUG:[$tag] $message")
    }

    override fun info(message: String?, throwable: Throwable?) {
        println("INFO:[$tag] $message")
    }

    override fun warn(message: String?, throwable: Throwable?) {
        println("WARN:[$tag] $message")
    }

    override fun error(message: String?, throwable: Throwable?) {
        println("ERROR:[$tag] $message")
    }

    override fun isTraceEnabled(): Boolean {
        return true
    }

    override fun isDebugEnabled(): Boolean {
        return true
    }

    override fun isInfoEnabled(): Boolean {
        return true
    }

    override fun isWarnEnabled(): Boolean {
        return true
    }

    override fun isErrorEnabled(): Boolean {
        return true
    }
}