package com.heyanle.easy_bangumi_cm.plugin.api.base

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


/**
 * Created by HeYanLe on 2024/12/8 21:34.
 * https://github.com/heyanLE
 */

sealed class SourceResult<T> {

    companion object {
        fun <T> ok(data: T) = Ok(data)

        fun <T> error(throwable: Throwable) =
            Error<T>(null, throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null) =
            Error<T>(errorMsg, throwable)
    }

    data class Ok<T>(val data: T) : SourceResult<T>()

    data class Error<T>(val msg: String?, val error: Throwable?) : SourceResult<T>()

}

suspend fun <T : Any, R> T.withResult(context: CoroutineContext? = null, block: suspend T.() -> R): SourceResult<R> {
    return try {
        if (context != null)
            withContext(context) {
                SourceResult.ok(block())
            }
        else {
            SourceResult.ok(block())
        }
    } catch (e: SourceException) {
        SourceResult.error(e.msg, e)
    } catch (e: Throwable) {
        SourceResult.error(e)
    }
}


