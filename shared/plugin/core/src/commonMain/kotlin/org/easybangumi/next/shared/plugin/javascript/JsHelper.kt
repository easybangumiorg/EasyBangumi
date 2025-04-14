package org.easybangumi.next.shared.plugin.javascript

import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.Source
import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

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
    val FIRST_LINE_MARK = "easy_bangumi_cm.jsc".encodeUtf8()

    fun isSourceCry(source: BufferedSource): Boolean {
        return source.peek().use {
            val mark = runCatching { it.readByteString(FIRST_LINE_MARK.size.toLong()) }.getOrElse { ByteString.EMPTY }
            return mark == FIRST_LINE_MARK
        }
    }


    fun getManifestFromNormal(source: BufferedSource) : Map<String, String> {
        try {

            val map = mutableMapOf<String, String>()
            while (true) {
                val line = source.readUtf8Line() ?: break
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

    fun getManifestFromCry(source: BufferedSource) : Map<String, String> {
        try {
            source.readUtf8(FIRST_LINE_MARK.size.toLong())
            val count = source.readInt()
            val buffer = Buffer()
            source.read(buffer, count.toLong())
            val map = mutableMapOf<String, String>()
            while (true) {
                val line = buffer.readUtf8Line() ?: break
                val kv = line.split(":", limit = 2)
                if (kv.size == 2) {
                    map[kv[0]] = kv[1]
                }
            }
            return map
        } catch (e: Exception) {
            return emptyMap()
        }
    }
}