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
): FileHelper<T> {

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
        StoreScope.launch(StoreSingleDispatcher) {
            initJob.join()
            dataFlow.update { t }
            saveToFile(t)
        }
    }

    override suspend fun setAndWait(t: T) {
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
        return dataFlow.asStateFlow()
    }

    private fun innerLoad(): Job {
        return StoreScope.launch {
            val data = loadFromFile()
            dataFlow.update { data }
        }
    }

    private fun loadFromFile(): T {
        val dataFile = dataFile ?: return def
        val bkFile = bkFile ?: return def

        var realFile = dataFile
        if (dataFile.exists()) {
            if (bkFile.exists()) {
                bkFile.delete()
            }
        } else {
            realFile = bkFile
        }

        if (!realFile.exists()) {
            return def
        }

        val res = realFile.openSource().buffer().use {
            deserializer(it) ?: def
        }
        return res
    }

    private fun saveToFile(t: T) {
        dataFile?.delete()
        val bk = bkFile
        bk ?: return
        bk.openSink(false).buffer().use {
            serializer(t, it)
            it.flush()
        }
        dataFile?.delete()
        bk.renameTo(dataFileName)
    }


    abstract fun suffix(): String
    abstract fun serializer(data: T, sink: BufferedSink)
    abstract fun deserializer(source: BufferedSource): T?



}