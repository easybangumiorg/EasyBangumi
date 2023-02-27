package com.heyanle.bangumi_source_api.api

import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

    // fun components(): List<Component>

    /**
     * 获取拆分 Source，默认不拆分
     * SearchSource、PageSource 等
     */
    fun sources(): List<Source> = emptyList()


}

suspend fun <T> withResult(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> T
): SourceResult<T> {
    return try {
        withContext(context) {
            SourceResult.Complete(block())
        }
    } catch (e: ParserException) {
        SourceResult.Error<T>(e, true)
    } catch (e: Exception) {
        SourceResult.Error<T>(e, false)
    }

}

class ParserException(
    override val message: String?
) : Exception()