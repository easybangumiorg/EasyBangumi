package org.easybangumi.next.lib.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


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

    class None<T> : DataState<T>()

    class Loading<T>(
        val loadingMsg: String = ""
    ) : DataState<T>()

    data class Ok<T>(
        val data: T
    ) : DataState<T>()

    data class Error<T>(
        val errorMsg: String,
        val throwable: Throwable?,
        val isEmpty: Boolean = false,
    ) : DataState<T>()

    companion object {

        fun <T> none() = None<T>()

        fun <T> loading() = Loading<T>()

        fun <T> ok(data: T) = Ok(data)

        fun <T> error(throwable: Throwable) =
            Error<T>(throwable.message ?: "", throwable)

        fun <T> error(errorMsg: String, throwable: Throwable? = null) =
            Error<T>(errorMsg, throwable)

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