package com.heyanle.easybangumi4.base

import com.heyanle.easybangumi4.plugin.api.SourceResult

/**
 * Created by HeYanLe on 2023/8/13 16:36.
 * https://github.com/heyanLE
 */
sealed class DataResult<T> {

    abstract val isCache: Boolean

    class Loading<T> : DataResult<T>() {
        override val isCache: Boolean = false
    }

    data class Ok<T>(
        val data: T,
        override val isCache: Boolean = false,
    ) : DataResult<T>()

    data class Error<T>(
        val errorMsg: String,
        val throwable: Throwable?,
    ) : DataResult<T>() {
        override val isCache: Boolean = false
    }

    companion object {
        fun <T> ok(data: T, isCache: Boolean = false) = DataResult.Ok(data, isCache)

        fun <T> error(throwable: Throwable) =
            DataResult.Error<T>(throwable.message ?: "", throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null) =
            DataResult.Error<T>(errorMsg, throwable)
    }

    fun okOrNull () : T? {
        return when (this) {
            is Ok -> data
            else -> null
        }
    }

    inline fun onOK(block: (T) -> Unit): DataResult<T> {
        if (this is DataResult.Ok) {
            block(data)
        }
        return this
    }

    inline fun onError(block: (DataResult.Error<T>) -> Unit): DataResult<T> {
        if (this is Error) {
            block(this)
        }
        return this
    }

    inline fun <R> mapOK(block: (T) -> R): R? {
        return when (this) {
            is DataResult.Ok -> {
                block(data)
            }

            is DataResult.Error -> {
                null
            }

            else -> {
                null
            }
        }
    }

    inline fun <R> mapError(block: (DataResult.Error<T>) -> R): R? {
        return when (this) {
            is DataResult.Ok -> {
                null
            }

            is DataResult.Error -> {
                block(this)
            }

            else -> {
                null
            }
        }
    }

}

fun <T> SourceResult<T>.toDataResult(): DataResult<T> =
    when (this) {
        is SourceResult.Complete -> {
            DataResult.ok(data, isCache)
        }

        is SourceResult.Error -> {
            DataResult.error(throwable)
        }
    }

public inline fun <T, R> DataResult<T>.map(transform: (value: T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Ok -> {
            DataResult.ok(transform(data), isCache)
        }

        is DataResult.Error -> {
            DataResult.error<R>(errorMsg = errorMsg, throwable)
        }
        is DataResult.Loading -> {
            DataResult.Loading()
        }
    }
