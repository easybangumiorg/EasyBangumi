package org.easybangumi.next.shared.source.bangumi.model

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import org.easybangumi.next.lib.utils.DataState

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

sealed class BgmRsp<T> {
    data class Success<T>(
        val code: Int,
        val data: T,
        val raw: String? = null
    ) : BgmRsp<T>() {
        override fun toString(): String {
            return "Success(code=$code, data=$data)"
        }
    }
    data class Error<T>(
        val code: Int,
        val isProxy: Boolean = false,
        val url: String? = null,
        val title: String? = null,
        val description: String? = null,
        val details: String? = null,
        val raw: String? = null,
        val throwable: Throwable? = null,
    ) : BgmRsp<T>() {
        companion object {
            const val INNER_ERROR_CODE = -1
        }

        fun isTimeout(): Boolean {
            return code == INNER_ERROR_CODE && (throwable is SocketTimeoutException || throwable is ConnectTimeoutException || throwable?.message?.contains("timeout") == true)
        }

        override fun toString(): String {
            return "Error(code=$code, title=$title, description=$description, details=$details, throwable=$throwable)"
        }


    }

    fun throwIfError() {
        (this as? Error<*>)?.throwable?.let {
            throw it
        }
    }


    fun toDataState(): DataState<T> {
        return when (this) {
            is Success -> DataState.Ok(data)
            is Error -> {
                DataState.Error(
                    errorMsg = "code: $code msg: ${title ?: description ?: details ?: raw?.substring(0, 100.coerceAtMost(raw.length)) ?: "Unknown error"}" ,
                    throwable = throwable,
                )
            }
        }
    }


}

class BgmNetException(
    val code: Int,
    val url: String? = null,
    val title: String? = null,
    val description: String? = null,
    val details: String? = null,
    val netCause: Throwable? = null,
) : Exception(
    "BgmNetException(code=$code, url=${url} title=$title, description=$description, details=$details)",
    netCause
) {

    val rsp = BgmRsp.Error<Any>(
        code = code,
        url = url,
        title = title,
        description = description,
        details = details,
        throwable = netCause
    )
    override fun toString(): String {
        return "BgmNetException(code=$code, url=${url}, title=$title, description=$description, details=$details, cause=${cause?.message})"
    }
}