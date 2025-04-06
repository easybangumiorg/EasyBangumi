package org.easybangumi.next.lib.logger

import org.apache.logging.log4j.core.config.Configurator
import org.slf4j.LoggerFactory
import kotlin.math.log


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


class LoggerWrapper(
    private val tag: String,
): Logger {
    private val logger: org.slf4j.Logger by lazy {
        LoggerFactory.getLogger(tag)
    }

    override fun isTraceEnabled(): Boolean {
        return logger.isTraceEnabled
    }

    override fun trace(message: String?, throwable: Throwable?) {
        logger.trace(message.withThreadName(), throwable)
    }

    override fun isDebugEnabled(): Boolean {
        return logger.isDebugEnabled
    }

    override fun debug(message: String?, throwable: Throwable?) {
        logger.debug(message.withThreadName(), throwable)
    }

    override fun isInfoEnabled(): Boolean {
        return logger.isInfoEnabled
    }

    override fun info(message: String?, throwable: Throwable?) {
        logger.info(message.withThreadName(), throwable)
    }

    override fun isWarnEnabled(): Boolean {
        return logger.isWarnEnabled
    }

    override fun warn(message: String?, throwable: Throwable?) {
        logger.warn(message.withThreadName(), throwable)
    }

    override fun isErrorEnabled(): Boolean {
        return logger.isErrorEnabled
    }

    override fun error(message: String?, throwable: Throwable?) {
        logger.error(message.withThreadName(), throwable)
    }

    // [ThreadName]message
    private fun String?.withThreadName(): String {
        val threadName = Thread.currentThread().name
        return "[$threadName]$this"
    }
}


actual fun Any.logger(): Logger {
    val tag = this::class.java.canonicalName?.removePrefix("org.easybangumi.next.")?:""
    return logger(tag)
}

actual fun logger(tag: String): Logger {
    return LoggerWrapper(tag)
}