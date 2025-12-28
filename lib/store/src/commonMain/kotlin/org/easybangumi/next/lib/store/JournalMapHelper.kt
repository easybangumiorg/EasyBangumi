package org.easybangumi.next.lib.store

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.buffer
import okio.use
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import kotlin.concurrent.Volatile

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
 *
 * 在一个文件夹里存储 kv 结构，目前只能存 String，且无法存空字符串，存空字符串会被视为删除，需要外界自行转换
 * 使用 journal 缓存日志的形式保证数据稳定性
 * 最极端情况能保证只丢失最后一次写入记录
 */

class JournalMapHelper(
    private val folder: UFD,
    private val name: String,
) {

    companion object {
        // 当写入记录条数为 map 数据条数多少倍时触发合并
        const val LOAD_FACTORY = 1.5f
    }

    data class Line(
        val key: String,
        val value: String?,
    )

    private val mapFlow = MutableStateFlow<Map<String, String>>(emptyMap())

    private val folderFile: UniFile? by lazy {
        UniFileFactory.fromUFD(folder)
    }

    private val dataFileName = "$name.journal"
    private val bkFileName = "$name.journal.bk}"

    @Volatile
    private var lastLineCount = 0

    private val dataFile: UniFile?
        get() = folderFile?.child(dataFileName)
    private val bkFile: UniFile?
        get() = folderFile?.child(bkFileName)

    private val initJob: Job by lazy {
        innerLoad()
    }

    // === api ===

    suspend fun map(): Map<String, String> {
        initJob.join()
        return mapFlow.value
    }

    fun mapSync(): Map<String, String> {
        runBlocking {
            initJob.join()
        }
        return mapFlow.value
    }

    suspend fun isSet(key: String): Boolean {
        initJob.join()
        return mapFlow.value[key] != null
    }

    fun isSetSync(key: String): Boolean {
        runBlocking {
            initJob.join()
        }
        return mapFlow.value[key] != null
    }

    fun put(key: String, value: String) {
        StoreScope.launch(StoreSingleDispatcher) {
            initJob.join()
            mapFlow.apply {
                while (true) {
                    val prevValue = this.value
                    val nextValue = prevValue + (key to value)
                    if (compareAndSet(prevValue, nextValue)) {
                        saveOrCombine(nextValue, key to value)
                        break
                    }
                }
            }
        }
    }

    suspend fun putAndWait(key: String, value: String) {
        withContext(StoreSingleDispatcher) {
            initJob.join()
            mapFlow.apply {
                while (true) {
                    val prevValue = this.value
                    val nextValue = prevValue + (key to value)
                    if (compareAndSet(prevValue, nextValue)) {
                        saveOrCombine(nextValue, key to value)
                        break
                    }
                }
            }
        }
    }

    fun remove(key: String) {
        StoreScope.launch(StoreSingleDispatcher) {
            initJob.join()
            mapFlow.apply {
                while (true) {
                    val prevValue = this.value
                    val nextValue = prevValue - key
                    if (compareAndSet(prevValue, nextValue)) {
                        saveOrCombine(nextValue, key to null)
                        break
                    }
                }
            }
        }
    }

    suspend fun removeAndWait(key: String) {
        withContext(StoreSingleDispatcher) {
            initJob.join()
            mapFlow.apply {
                while (true) {
                    val prevValue = this.value
                    val nextValue = prevValue - key
                    if (compareAndSet(prevValue, nextValue)) {
                        saveOrCombine(nextValue, key to null)
                        break
                    }
                }
            }
        }
    }

    fun getSync(key: String): String {
        runBlocking {
            initJob.join()
        }
        return mapFlow.value[key] ?: ""
    }

    suspend fun get(key: String): String {
        initJob.join()
        return mapFlow.value[key] ?: ""
    }

    fun flowMap(): StateFlow<Map<String, String>> {
        return mapFlow.asStateFlow()
    }


    private fun innerLoad(): Job {
        return StoreScope.launch(StoreSingleDispatcher) {
            val map = loadFromFile()
            mapFlow.update { map }
        }
    }

    private fun saveOrCombine(map: Map<String, String>, line: Pair<String, String?>) {
        lastLineCount ++
        if (lastLineCount > map.size * LOAD_FACTORY) {
            val dataFile = dataFile ?: return
            val bkFile = bkFile ?: return
            bkFile.delete()
            bkFile.openSink(false).buffer().use {
                for ((key, value) in map) {
                    val line = Line(key, value)
                    it.writeUtf8(jsonSerializer.serialize(line))
                    it.writeUtf8("\n")
                }
                it.flush()
            }
            dataFile.delete()
            bkFile.renameTo(dataFileName)
            bkFile.delete()
            lastLineCount = map.size
        } else {
            bkFile?.delete()
            dataFile?.openSink(true)?.buffer()?.use { sink ->
                val line = Line(line.first, line.second)
                sink.writeUtf8(jsonSerializer.serialize(line))
                sink.writeUtf8("\n")
                sink.flush()
            }
        }
    }

    private fun loadFromFile(): Map<String, String> {
        val dataFile = dataFile ?: return emptyMap()
        val bkFile = bkFile ?: return emptyMap()
        var realFile = dataFile
        if (dataFile.exists()) {
            if (bkFile.exists()) {
                bkFile.delete()
            }
        } else {
            realFile = bkFile
        }
        if (!realFile.exists()) {
            return emptyMap()
        }
        return realFile.openSource().buffer().use { source ->
            val map = HashMap<String, String>()
            var lineCount = 0
            while(true) {
                val line = source.readUtf8Line() ?: break
                val li = jsonSerializer.deserialize<Line>(line, defaultValue = null) ?: continue
                lineCount ++
                li.value ?.let {
                    map[li.key] = it
                }
            }
            lastLineCount = lineCount
            map
        }
    }



}