package com.heyanle.easy_bangumi_cm.base

/**
 * Created by HeYanLe on 2023/8/13 16:36.
 * https://github.com/heyanLE
 */
sealed class DataResult<T> {

    class Loading<T> : DataResult<T>()

    data class Ok<T>(
        val data: T
    ) : DataResult<T>()

    data class Error<T>(
        val errorMsg: String,
        val throwable: Throwable?,
    ) : DataResult<T>()

    companion object {
        fun <T> ok(data: T) = Ok(data)

        fun <T> error(throwable: Throwable) =
            Error<T>(throwable.message ?: "", throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null) =
            Error<T>(errorMsg, throwable)
    }

    fun okOrNull () : T? {
        return when (this) {
            is Ok -> data
            else -> null
        }
    }

    inline fun onOK(block: (T) -> Unit): DataResult<T> {
        if (this is Ok) {
            block(data)
        }
        return this
    }

    inline fun onError(block: (Error<T>) -> Unit): DataResult<T> {
        if (this is Error) {
            block(this)
        }
        return this
    }

    inline fun <R> mapOK(block: (T) -> R): R? {
        return when (this) {
            is Ok -> {
                block(data)
            }

            is Error -> {
                null
            }

            else -> {
                null
            }
        }
    }

    inline fun <R> mapError(block: (Error<T>) -> R): R? {
        return when (this) {
            is Ok -> {
                null
            }

            is Error -> {
                block(this)
            }

            else -> {
                null
            }
        }
    }

}

public inline fun <T, R> DataResult<T>.map(transform: (value: T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Ok -> {
            DataResult.ok(transform(data))
        }

        is DataResult.Error -> {
            DataResult.error<R>(errorMsg = errorMsg, throwable)
        }
        is DataResult.Loading -> {
            DataResult.Loading()
        }
    }