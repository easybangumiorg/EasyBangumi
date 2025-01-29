package com.heyanle.easy_bangumi_cm.common.plugin.core.utils

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

/**
 * jsc 文件数据结构
 * 1. 标记
 * 2. 元数据长度（占 4 个字节）
 * 3. 元数据（明文，长度记录在 2）, key:value 换行分割并且 key 和 value 都需要 urlEncode
 * 4. 加密数据
 *
 * js 元数据结构
 * //@key value
 * //@key value
 * ...
 * Created by heyanlin on 2024/12/18.
 */
object JsHelper {

    val CHUNK_SIZE = 1024

    // jsc 文件标记
    val FIRST_LINE_MARK = "easy_bangumi_cm.jsc".toByteArray()

    fun getManifest(file: File): Map<String, String> {
        file.inputStream().use {
            return getManifest(it)
        }
    }

    fun getManifest(inputStream: InputStream): Map<String, String> {
        val inp = inputStream.buffered()
        return if (isCryptJs(inp)) {
            getManifestFromCry(inp)
        } else {
            getManifestFromNormal(inp)
        }
    }

    fun getManifestFromNormal(inputStream: InputStream) : Map<String, String> {
        try {
            val i = BufferedInputStream(inputStream).bufferedReader().lineSequence()
            val map = mutableMapOf<String, String>()
            for (line in i) {
                if (!line.startsWith("//")) {
                    break
                }
                val atIndex = line.indexOf("@")
                val spaceIndex = line.indexOf(" ")
                if (atIndex == -1 || spaceIndex == -1) {
                    continue
                }
                val key = line.substring(2, atIndex)
                val value = line.substring(atIndex + 1, spaceIndex)
                map[key] = value
            }
            return map
        } catch (e: Exception) {
            return emptyMap()
        }
    }


    fun getManifestFromCry(inputStream: InputStream) : Map<String, String> {
        try {
            inputStream.mark(Int.MAX_VALUE)
            inputStream.skip(FIRST_LINE_MARK.size.toLong())
            val count = inputStream.readInt()
            val buffer = ByteArray(count)
            inputStream.read(buffer)
            val i = ByteArrayInputStream(buffer).bufferedReader().lineSequence()
            val map = mutableMapOf<String, String>()
            i.forEach {
                val kv = it.split(":", limit = 2)
                if (kv.size == 2) {
                    map[kv[0]] = kv[1]
                }
            }
            inputStream.reset()
            return map
        } catch (e: Exception) {
            return emptyMap()
        }
    }


    fun isCryptJs(file: File): Boolean {
        try {
            file.inputStream().use {
                return isCryptJs(it)
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun isCryptJs(inputStream: InputStream): Boolean {
        try {
            inputStream.mark(FIRST_LINE_MARK.size)
            val buffer = ByteArray(FIRST_LINE_MARK.size)
            inputStream.read(buffer)
            inputStream.reset()
            return buffer.contentEquals(FIRST_LINE_MARK)
        } catch (e: Exception) {
            return false
        }
    }
}