package com.heyanle.easy_bangumi_cm.shared.utils.file_helper

import com.heyanle.easy_bangumi_cm.shared.utils.jsonTo
import com.heyanle.easy_bangumi_cm.shared.utils.toJson
import com.heyanle.easy_bangumi_cm.unifile.IUniFile
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
class JsonFileHelper<T : Any>(
    folder: IUniFile,
    name: String,
    def: T,
    scope: CoroutineScope,
    private val type: Type,
): BaseFileHelper<T>(folder, "${name}${FILE_SUFFIX}", def, scope) {

    companion object {
        const val FILE_SUFFIX = ".json"
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