package com.heyanle.bangumi_source_api.api

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.component.Component
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by HeYanLe on 2023/2/18 21:38.
 * https://github.com/heyanLE
 */
@Keep
interface Source {

    /**
     * Must be unique
     */
    val key: String

    val label: String

    val version: String

    val versionCode: Int

    val describe: String?

    /**
     * 获取组件
     */
    fun components(): List<Component> = emptyList()


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
    override val message: String?
) : Exception()