package com.heyanle.easy_bangumi_cm.base.utils.file_helper

import com.heyanle.easy_bangumi_cm.base.utils.jsonTo
import com.heyanle.easy_bangumi_cm.base.utils.toJson
import com.heyanle.easy_bangumi_cm.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 按行读取 jsonl 文件
 * 暂不支持分页
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
class JsonlFileHelper<T : Any>(
    folder: UniFile,
    name: String,
    scope: CoroutineScope,
    private val type: Type,
): BaseFileHelper<List<T>>(folder, "${name}$FILE_SUFFIX", emptyList(), scope) {

    companion object {
        const val FILE_SUFFIX = ".jsonl"
    }

    override fun load(inputStream: InputStream): List<T> {
        return inputStream.bufferedReader().lineSequence().mapNotNull {
            it.jsonTo<T>(type)
        }.toList()
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