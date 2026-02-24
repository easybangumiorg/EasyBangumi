package com.heyanle.easybangumi4.source_api

import androidx.annotation.Keep
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Keep
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

@Keep
suspend fun <T> withResult(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> T
): SourceResult<T> {
    return try {
        withContext(context) {
            SourceResult.Complete(block())
        }
    } catch (e: ParserException) {
        e.printStackTrace()
        SourceResult.Error<T>(e, true)
    } catch (e: Exception) {
        e.printStackTrace()
        SourceResult.Error<T>(e, false)
    }

}

@Keep
class ParserException(
    override val message: String?,
    val exception: Exception? = null
) : Exception()