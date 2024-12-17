package com.heyanle.easy_bangumi_cm.base.utils.file_helper

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.jsonTo
import com.heyanle.easy_bangumi_cm.base.utils.toJson
import com.heyanle.easy_bangumi_cm.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * Created by heyanlin on 2024/12/17.
 */
class JsonFileHelper<T : Any>(
    folder: UniFile,
    name: String,
    def: T,
    scope: CoroutineScope,
    private val type: Type,
) : AbsFileHelper<T>(folder, "${name}${FILE_SUFFIX}", def, scope) {

    companion object {
        const val FILE_SUFFIX = ".json"

        inline fun <reified T: Any> from(
            folder: UniFile,
            name: String,
            def: T,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + CoroutineProvider.io)
        ): JsonFileHelper<T> {
            return JsonFileHelper(folder, name, def, scope, T::class.java)
        }
    }

    override fun load(inputStream: InputStream): T? {
        return inputStream.bufferedReader().readText().jsonTo(type)
    }

    override fun save(t: T, outputStream: OutputStream): Boolean {
        return kotlin.runCatching {
            outputStream.bufferedWriter().use {
                it.write(t.toJson(type))
                it.flush()
            }
        }.onFailure {
            it.printStackTrace()
        }.isSuccess
    }

}
