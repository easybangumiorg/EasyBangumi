package com.heyanle.easy_bangumi_cm.base.utils.file_helper

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.moshi.jsonTo
import com.heyanle.easy_bangumi_cm.base.utils.moshi.toJson
import com.heyanle.lib.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * Created by heyanlin on 2024/12/17.
 */
class JsonlFileHelper<T : Any>(
    folder: UniFile,
    name: String,
    scope: CoroutineScope,
    private val type: Type,
) : AbsFileHelper<List<T>>(folder, "${name}${FILE_SUFFIX}", emptyList(), scope) {

    companion object {
        const val FILE_SUFFIX = ".jsonl"

        inline fun <reified T: Any> from(
            folder: UniFile,
            name: String,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + CoroutineProvider.io)
        ): JsonlFileHelper<T> {
            return JsonlFileHelper<T>(folder, name,  scope,  T::class.java)
        }
    }

    override fun load(inputStream: InputStream): List<T> {
        return inputStream.bufferedReader().use {
            it.lineSequence().mapNotNull {
                it.jsonTo<T>(type)
            }.toList()
        }
    }

    override fun save(t: List<T>, outputStream: OutputStream): Boolean {
        return kotlin.runCatching {
            t.forEach {
                outputStream.bufferedWriter().use {
                    it.write(it.toJson(type))
                    it.newLine()
                }
            }
            true
        }.onFailure {
            it.printStackTrace()
        }.isSuccess
    }

}
