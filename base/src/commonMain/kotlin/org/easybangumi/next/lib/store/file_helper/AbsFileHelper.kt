package org.easybangumi.next.lib.store.file_helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.use
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.store.StoreScope
import org.easybangumi.next.lib.store.StoreSingleDispatcher
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.Global
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
 */
abstract class AbsFileHelper<T : Any>(
    private val folder: UFD,
    private val name: String,
    private val def: T,
    private val isLogger: Boolean = false,
): FileHelper<T> {

    private val logger = logger()

    private fun log(message: String) {
        if (isLogger) {
            logger.info(message)
        }
    }

//    companion object {
//        const val FINAL_MARK = "$\$final$$"
//    }

    private val dataFlow = MutableStateFlow<T>(def)


    private val folderFile: UniFile? by lazy {
        UniFileFactory.fromUFD(folder)
    }

    private val dataFileName = "$name.${suffix()}"
    private val bkFileName = "$name.bk.${suffix()}"

    private val dataFile: UniFile?
        get() = folderFile?.child(dataFileName)
    private val bkFile: UniFile?
        get() = folderFile?.child(bkFileName)

    private val initJob: Job by lazy {
        innerLoad()
    }

    override fun getSync(): T {
        runBlocking {
            initJob.join()
        }
        return dataFlow.value
    }

    override suspend fun get(): T {
        initJob.join()
        return dataFlow.value
    }

    override fun push(t: T) {
        log("push 调用: name=$name")
        StoreScope.launch(StoreSingleDispatcher) {
            initJob.join()
            dataFlow.update { t }
            saveToFile(t)
        }
    }

    override suspend fun setAndWait(t: T) {
        log("setAndWait 调用: name=$name")
        initJob.join()
        dataFlow.update { t }
        withContext(StoreSingleDispatcher) {
            saveToFile(t)
        }
    }

    override fun def(): T {
        return def
    }

    override fun flow(): StateFlow<T> {
        // 触发一次 init
        initJob
        return dataFlow.asStateFlow()
    }

    private fun innerLoad(): Job {
        return StoreScope.launch {
            log("初始化加载文件: name=$name")
            val data = loadFromFile()
            dataFlow.update { data }
            log("初始化加载完成: name=$name")
        }
    }

    private fun loadFromFile(): T {
        log("开始加载文件: name=$name, dataFileName=$dataFileName, bkFileName=$bkFileName")
        val dataFile = dataFile ?: run {
            log("数据文件为空，返回默认值")
            return def
        }
        val bkFile = bkFile ?: run {
            log("备份文件为空，返回默认值")
            return def
        }

        var realFile = dataFile
        if (dataFile.exists()) {
            log("数据文件存在: ${dataFile.getUri()}")
            if (bkFile.exists()) {
                log("删除备份文件: ${bkFile.getUri()}")
                bkFile.delete()
            }
        } else {
            log("数据文件不存在，尝试使用备份文件")
            realFile = bkFile
        }

        if (!realFile.exists()) {
            log("文件不存在，返回默认值: ${realFile.getUri()}")
            return def
        }

        log("从文件读取数据: ${realFile.getUri()}")
        val res = realFile.openSource().buffer().use {
            deserializer(it) ?: def
        }
        log("文件加载完成: name=$name, 数据已读取")
        return res
    }

    private fun saveToFile(t: T) {
        log("开始保存文件: name=$name, dataFileName=$dataFileName, bkFileName=$bkFileName")
        dataFile?.delete()
        val bk = bkFile
        bk ?: run {
            log("备份文件为空，无法保存")
            return
        }
        log("写入备份文件: ${bk.getUri()}")
        bk.openSink(false).buffer().use {
            serializer(t, it)
            it.flush()
        }
        log("备份文件写入完成，删除旧数据文件")
        dataFile?.delete()
        log("将备份文件重命名为数据文件: $dataFileName")
        bk.renameTo(dataFileName)
        log("文件保存完成: name=$name")
    }


    abstract fun suffix(): String
    abstract fun serializer(data: T, sink: BufferedSink)
    abstract fun deserializer(source: BufferedSource): T?



}