package com.heyanle.bangumi_source_api.api2

import com.heyanle.bangumi_source_api.api2.component.Component

/**
 * Created by HeYanLe on 2023/2/18 21:38.
 * https://github.com/heyanLE
 */
interface Source {

    /**
     * Must be unique
     */
    val key: String

    val label: String

    val version: String

    val versionCode: Int

    val describe: String?

    fun components(): List<Component>


    sealed class SourceResult<T> {
        data class Complete<T>(
            val data: T
        ) : SourceResult<T>()

        data class Error<T>(
            val throwable: Throwable,
            val isParserError: Boolean = false
        ) : SourceResult<T>()

        inline fun complete(block: (Complete<T>) -> Unit): SourceResult<T> {
            if (this is Complete) {
                block(this)
            }
            return this
        }

        inline fun error(block: (Error<T>) -> Unit): SourceResult<T> {
            if (this is Error) {
                block(this)
            }
            return this
        }
    }

}