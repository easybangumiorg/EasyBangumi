package org.easybangumi.next.shared.plugin

import kotlinx.coroutines.withContext
import org.easybangumi.next.lib.utils.DataState
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


    fun isOk() : Boolean {
        return this is Ok
    }

    fun isError() : Boolean {
        return this is Error
    }


    fun okOrNull () : T? {
        return when (this) {
            is Ok -> data
            else -> null
        }
    }

    inline fun onOK(block: (T) -> Unit): SourceResult<T> {
        if (this is Ok) {
            block(data)
        }
        return this
    }

    inline fun onError(block: (Error<T>) -> Unit): SourceResult<T> {
        if (this is Error) {
            block(this)
        }
        return this
    }

}

fun <T> SourceResult<T>.toDataState(): DataState<T> {
    return when (this) {
        is SourceResult.Ok -> DataState.ok(data)
        is SourceResult.Error -> DataState.error(msg ?: "", error)
    }
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


