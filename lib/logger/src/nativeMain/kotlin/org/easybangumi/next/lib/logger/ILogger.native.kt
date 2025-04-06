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

actual interface Logger {
    actual fun isTraceEnabled(): Boolean
    actual fun trace(message: String?, throwable: Throwable?)
    actual fun isDebugEnabled(): Boolean
    actual fun debug(message: String?, throwable: Throwable?)
    actual fun isInfoEnabled(): Boolean
    actual fun info(message: String?, throwable: Throwable?)
    actual fun isWarnEnabled(): Boolean
    actual fun warn(message: String?, throwable: Throwable?)
    actual fun isErrorEnabled(): Boolean
    actual fun error(message: String?, throwable: Throwable?)

}

actual fun Any.logger(): Logger {
    TODO("Not yet implemented")
}

actual fun logger(tag: String): Logger {
    TODO("Not yet implemented")
}