package com.heyanle.bangumi_source_api.api

sealed class SourceResult<T> {
        data class Complete<T>(
            val data: T
        ) : SourceResult<T>()

        data class Error<T>(
            val throwable: Throwable,
            val isParserError: Boolean = false
        ) : SourceResult<T>()

        inline fun complete(block: (SourceResult.Complete<T>) -> Unit): SourceResult<T> {
            if (this is Complete) {
                block(this)
            }
            return this
        }

        inline fun error(block: (SourceResult.Error<T>) -> Unit): SourceResult<T> {
            if (this is Error) {
                block(this)
            }
            return this
        }
    }