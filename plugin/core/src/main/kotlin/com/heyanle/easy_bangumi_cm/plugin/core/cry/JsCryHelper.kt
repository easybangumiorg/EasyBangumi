package com.heyanle.easy_bangumi_cm.plugin.core.cry

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.net.URLDecoder

/**
 * js 文件加密解密
 * 文件数据结构
 * 1. 首行标记
 * 2. 元数据长度（占 4 个字节）
 * 3. 元数据（明文，长度记录在 2）, key:value 换行分割并且 key 和 value 都需要 urlEncode
 * 4. 加密数据
 * Created by heyanlin on 2024/12/18.
 */
object JsCryHelper {

    val CHUNK_SIZE = 1024

    // 加密 js 文件首行
    val FIRST_LINE_MARK = "easy_bangumi_cm.jsc".toByteArray()


    fun getManifest(file: File): Map<String, String>? {
        file.inputStream().use {
            return getManifest(it)
        }
    }

    // 元数据存明文
    fun getManifest(inputStream: InputStream): Map<String, String>? {
        try {
            if (!isCryJs(inputStream)) {
                return null
            }
            val count = inputStream.readInt()
            val buffer = ByteArray(count)
            inputStream.read(buffer)
            val i = ByteArrayInputStream(buffer).bufferedReader().lineSequence()
            val map = mutableMapOf<String, String>()
            i.forEach {
                val kv = it.split(":", limit = 2)
                if (kv.size == 2) {
                    map[URLDecoder.decode(kv[0], "utf-8")] = URLDecoder.decode(kv[1], "utf-8")
                }
            }
            return map
        } catch (e: Exception) {
            return null
        }
    }

    fun isCryJS(file: File): Boolean {
        try {
            file.inputStream().use {
                return isCryJs(it)
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun isCryJs(inputStream: InputStream): Boolean {
        try {
            val buffer = ByteArray(FIRST_LINE_MARK.size)
            inputStream.read(buffer)
            return buffer.contentEquals(FIRST_LINE_MARK)
        } catch (e: Exception) {
            return false
        }
    }

    private fun InputStream.readInt(): Int {
        val readBuffer = ByteArray(Int.SIZE_BYTES)
        this.read(readBuffer)
        return ((readBuffer[0].toInt() shl 24) +
                ((readBuffer[1].toInt() and 255) shl 16) +
                ((readBuffer[2].toInt() and 255) shl 8) +
                ((readBuffer[3].toInt() and 255) shl 0))
    }



}