package com.heyanle.easybangumi4.base

import androidx.webkit.internal.ApiFeature.T
import com.heyanle.easybangumi4.source_api.SourceResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by HeYanLe on 2023/8/13 16:36.
 * https://github.com/heyanLE
 */
sealed class DataResult<T> {

    class Ok<T>(
        val data: T
    ) : DataResult<T>()

    class Error<T>(
        val errorMsg: String,
        val throwable: Throwable?,
    ) : DataResult<T>()

    fun a(list: Flow<Int>) {
        list.map { }
    }

    companion object {
        fun <T> ok(data: T) = DataResult.Ok(data)

        fun <T> error(throwable: Throwable) =
            DataResult.Error<T>(throwable.message ?: "", throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null) =
            DataResult.Error<T>(errorMsg, throwable)
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
        }
    }

}

fun <T> SourceResult<T>.toDataResult(): DataResult<T> =
    when (this) {
        is SourceResult.Complete -> {
            DataResult.ok(data)
        }

        is SourceResult.Error -> {
            DataResult.error(throwable)
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
    }