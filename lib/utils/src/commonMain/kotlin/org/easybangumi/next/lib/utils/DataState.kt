package org.easybangumi.next.lib.utils

import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

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

sealed class DataState<T> {

    abstract val timestamp: Long
    abstract val cacheData: T?

    class None<T>(
        override val cacheData: T? = null,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : DataState<T>() {
        override fun toString(): String {
            return "None(timestamp=$timestamp)"
        }
    }

    class Loading<T>(
        val loadingMsg: String = "",
        override val cacheData: T? = null,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : DataState<T>() {
        override fun toString(): String {
            return "Loading(loadingMsg='$loadingMsg', timestamp=$timestamp)"
        }
    }

    class Ok<T>(
        val data: T,
        val isCache: Boolean = false,
        override val cacheData: T? = data,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : DataState<T>() {
        override fun toString(): String {
            return "Ok(data=$data, isCache=$isCache, timestamp=$timestamp)"
        }
    }

    class Error<T>(
        val errorMsg: String,
        val throwable: Throwable? = null,
        val isEmpty: Boolean = false,
        override val cacheData: T? = null,
        override val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : DataState<T>() {
        override fun toString(): String {
            return "Error(errorMsg='$errorMsg', throwable=$throwable, isEmpty=$isEmpty, timestamp=$timestamp)"
        }
    }

    companion object {

        fun <T> none() = None<T>()

        fun <T> loading() = Loading<T>()

        fun <T> loading(
            loadingMsg: String = "",
            cacheData: T? = null,
            timestamp: Long = Clock.System.now().toEpochMilliseconds()
        ) = Loading(loadingMsg, cacheData, timestamp)

        fun <T> ok(
            data: T,
            isCache: Boolean = false,
            timestamp: Long = Clock.System.now().toEpochMilliseconds()
        ) = Ok(data, isCache, timestamp = timestamp)

        fun <T> error(throwable: Throwable) =
            Error<T>(throwable.message ?: "", throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null, dataCache: T? = null, timestamp: Long = Clock.System.now().toEpochMilliseconds()) =
            Error<T>(errorMsg, throwable, false, dataCache, timestamp)

        fun <T> empty(errorMsg: String = "") =
            Error<T>(errorMsg, null, true)
    }

    @OptIn(ExperimentalContracts::class)
    fun isNone() : Boolean {
        contract {
            returns(true) implies (this@DataState is None)
        }
        return this is None
    }

    @OptIn(ExperimentalContracts::class)
    fun isOk() : Boolean {
        contract {
            returns(true) implies (this@DataState is Ok)
        }
        return this is Ok
    }

    @OptIn(ExperimentalContracts::class)
    fun isError() : Boolean {
        contract {
            returns(true) implies (this@DataState is Error)
        }
        return this is Error
    }

    @OptIn(ExperimentalContracts::class)
    fun isLoading() : Boolean {
        contract {
            returns(true) implies (this@DataState is Loading)
        }
        return this is Loading
    }

    fun okOrNull () : T? {
        return when (this) {
            is Ok -> data
            else -> null
        }
    }

    fun okOrCache(): T? {
        return okOrNull() ?: cacheData
    }

    inline fun onOK(block: (T) -> Unit): DataState<T> {
        if (this is Ok) {
            block(data)
        }
        return this
    }

    inline fun onLoading(block: (Loading<T>) -> Unit): DataState<T> {
        if (this is Loading) {
            block(this)
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

public inline fun <T, R> DataState<T>.mapWithState(transform: (value: T) -> DataState<R>): DataState<R> =
    when (this) {
        is DataState.Ok -> {
            transform(data)
        }

        is DataState.Error -> {
            DataState.error<R>(errorMsg = errorMsg, throwable)
        }
        is DataState.Loading -> {
            DataState.loading()
        }

        is DataState.None -> DataState.none<R>()
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
            DataState.loading()
        }

        is DataState.None -> DataState.none<R>()
    }


suspend fun <T : Any, R> T.withResult(context: CoroutineContext? = null, block: suspend T.() -> R): DataState<R> {
    return try {
        if (context != null)
            withContext(context) {
                DataState.ok(block())
            }
        else {
            DataState.ok(block())
        }
    } catch (e: DataStateException) {
        e.printStackTrace()
        if (e.isEmpty) {
            DataState.empty(e.errorMsg)
        } else {
            DataState.error(e.errorMsg, e.throwable)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        DataState.error(e.message ?: e.toString(), e)
    }
}

/**
 * 可预见的错误
 */
class DataStateException(
    val errorMsg: String,
    val throwable: Throwable? = null,
    val isEmpty: Boolean = false,
) : Exception(errorMsg, throwable)