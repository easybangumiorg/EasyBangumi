package com.heyanle.easy_bangumi_cm.shared.utils

/**
 * Created by HeYanLe on 2023/8/13 16:36.
 * https://github.com/heyanLE
 */
sealed class DataState<T> {

    class Loading<T> : DataState<T>()

    data class Ok<T>(
        val data: T
    ) : DataState<T>()

    data class Error<T>(
        val errorMsg: String,
        val throwable: Throwable?,
    ) : DataState<T>()

    companion object {
        fun <T> ok(data: T) = Ok(data)

        fun <T> error(throwable: Throwable) =
            Error<T>(throwable.message ?: "", throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null) =
            Error<T>(errorMsg, throwable)
    }

    fun isOk() : Boolean {
        return this is Ok
    }

    fun okOrNull () : T? {
        return when (this) {
            is Ok -> data
            else -> null
        }
    }

    inline fun onOK(block: (T) -> Unit): DataState<T> {
        if (this is Ok) {
            block(data)
        }
        return this
    }

    inline fun onError(block: (Error<T>) -> Unit): DataState<T> {
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

public inline fun <T, R> DataState<T>.map(transform: (value: T) -> R): DataState<R> =
    when (this) {
        is DataState.Ok -> {
            DataState.ok(transform(data))
        }

        is DataState.Error -> {
            DataState.error<R>(errorMsg = errorMsg, throwable)
        }
        is DataState.Loading -> {
            DataState.Loading()
        }
    }