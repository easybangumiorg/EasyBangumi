package com.heyanle.easy_bangumi_cm.shared.base



/**
 * Created by HeYanLe on 2024/12/3 0:15.
 * https://github.com/heyanLE
 */
var logger: Logger? = null

interface Logger {

    companion object {

        fun d(tag: String, msg: String) {
            logger?.d(tag, msg) ?: Inject.getOrNull<Logger>()?.d(tag, msg) ?: println("[$tag] $msg")
        }

        fun e(tag: String, msg: String, e: Throwable) {
            logger?.e(tag, msg, e) ?: koin.getOrNull<Logger>()?.d(tag, msg) ?: {
                println("[$tag] $msg")
                e.printStackTrace()
            }

        }

        fun i(tag: String, msg: String) {
            logger?.i(tag, msg) ?: koin.getOrNull<Logger>()?.d(tag, msg) ?: println("[$tag] $msg")
        }

        fun w(tag: String, msg: String) {
            logger?.w(tag, msg) ?: koin.getOrNull<Logger>()?.d(tag, msg) ?: println("[$tag] $msg")
        }

        fun v(tag: String, msg: String) {
            logger?.v(tag, msg) ?: koin.getOrNull<Logger>()?.d(tag, msg) ?: println("[$tag] $msg")
        }

        fun wtf(tag: String, msg: String) {
            logger?.wtf(tag, msg) ?: koin.getOrNull<Logger>()?.d(tag, msg) ?: println("[$tag] $msg")
        }


    }

    fun d(tag: String, msg: String)

    fun e(tag: String, msg: String, e: Throwable?)

    fun i(tag: String, msg: String)

    fun w(tag: String, msg: String)

    fun v(tag: String, msg: String)

    fun wtf(tag: String, msg: String)


}

fun Any.logi(msg: String) {
    Logger.i(this::class.simpleName ?: "Unknown", msg)
}

fun CharSequence.tlogi(tag: String) {
    Logger.i(tag, this.toString())
}