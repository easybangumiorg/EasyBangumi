package org.easybangumi.next.lib.logger

import org.slf4j.LoggerFactory
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

actual typealias Logger = org.slf4j.Logger


actual fun Any.logger(): Logger {
    val tag = this::class.java.canonicalName?.removePrefix("org.easybangumi.next.")?:""
    return logger(tag)
}

actual fun logger(tag: String): Logger {
    return LoggerFactory.getILoggerFactory().getLogger(tag)
}