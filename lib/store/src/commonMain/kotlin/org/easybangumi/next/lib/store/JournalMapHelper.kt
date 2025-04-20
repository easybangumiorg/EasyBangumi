package org.easybangumi.next.lib.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.buffer
import okio.use
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.coroutineProvider
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
    private val scope: CoroutineScope,
) {

    companion object {
        // 当写入记录条数为 map 数据条数多少倍时触发合并
        const val LOAD_FACTORY = 1.5f
    }

    data class Line(
        val key: String,
        val value: String,
    )

    @Volatile
    private var map: Map<String, String> = emptyMap()

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

    private var lastIOJob: Job? = null

    // TODO 并发
    private val setListener = mutableListOf<(Map<String, String>) -> Unit>()

    fun getSync(key: String, def: String): String {
        runBlocking {
            initJob.join()
        }
        return map[key] ?: def
    }

    suspend fun get(key: String, def: String): String {
        initJob.join()
        return map[key] ?: def
    }

    fun set(key: String, value: String) {
        scope.launch {
            initJob.join()
            scope.launch(coroutineProvider.single()) {
                bkFile?.delete()
                val mapSnapshot = HashMap(map)
                mapSnapshot[key] = value

                map = mapSnapshot
                if (mapSnapshot.size > lastLineCount * LOAD_FACTORY) {
                    combine(mapSnapshot)
                } else {
                    insert(key, value)
                }

                setListener.forEach { it(mapSnapshot) }
            }
        }
    }

    private suspend fun insert(key: String, value: String) {
        initJob.join()
        lastIOJob?.cancelAndJoin()
        lastIOJob = scope.launch {
            val dataFile = dataFile ?: return@launch
            bkFile?.delete()
            dataFile.openSink(true).buffer().use { sink ->
                val line = Line(key, value)
                sink.writeUtf8(jsonSerializer.serialize(line))
                sink.writeUtf8("\n")
            }
        }
    }

    private suspend fun combine(data: Map<String, String>) {
        initJob.join()
        lastIOJob?.cancelAndJoin()
        lastIOJob = scope.launch {
            val bkFile = bkFile ?: return@launch
            if (data.isNotEmpty()) {
                bkFile.delete()
                bkFile.openSink(false).buffer().use { sink ->
                    for (entry in data.entries) {
                        val line = Line(entry.key, entry.value)
                        sink.writeUtf8(jsonSerializer.serialize(line))
                        sink.writeUtf8("\n")
                    }
                }
                dataFile?.delete()
                bkFile.renameTo(dataFileName)
            } else {
                dataFile?.delete()
                bkFile?.delete()
            }
        }
    }

    private fun innerLoad(): Job {
        return scope.launch {
            val data = loadFromFile()
            withContext(coroutineProvider.single()) {
                map = data
                setListener.forEach { it(map) }
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
                map[li.key] = li.value
            }
            lastLineCount = lineCount
            map
        }
    }



}