package org.easybangumi.next.lib.logger

actual fun logger(tag: String): Logger {
    TODO("Not yet implemented")
}

actual fun Any.logger(): Logger {
    TODO("Not yet implemented")
}

actual interface Logger {
    actual fun trace(message: String?, throwable: Throwable?)
    actual fun debug(message: String?, throwable: Throwable?)
    actual fun info(message: String?, throwable: Throwable?)
    actual fun warn(message: String?, throwable: Throwable?)
    actual fun error(message: String?, throwable: Throwable?)
    actual fun isTraceEnabled(): Boolean
    actual fun isDebugEnabled(): Boolean
    actual fun isInfoEnabled(): Boolean
    actual fun isWarnEnabled(): Boolean
    actual fun isErrorEnabled(): Boolean
}