package org.easybangumi.next.lib.store.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.use
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.DataRepository
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.MutableDataRepository

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
abstract class FileMutableAbsRepository <T: Any>(
    folder: UFD,
    val name: String,
    val scope: CoroutineScope,
): MutableDataRepository<T> {

    protected val logger = logger(this.toString())

    private val _flow = MutableStateFlow<DataState<T>>(DataState.Companion.none())
    override val flow: StateFlow<DataState<T>> = _flow

    private val folder by lazy {
        UniFileFactory.fromUFD(folder)
    }
    private val file by lazy {
        this.folder?.child(name)
    }
    private val tempFile by lazy {
        this.folder?.child("$name.temp")
    }

    abstract suspend fun fetchRemoteData(): DataState<T>

    abstract fun save(data: T, sink: BufferedSink)
    abstract fun load(source: BufferedSource): T?


    init {
        scope.launch {
            // 1. 先加载缓存
            val file = file ?: return@launch
            if (file.exists()) {
                try {
                    file.openSource().buffer().use {
                        val d = load(it)
                        logger.info("load cache data: $d")
                        if (d != null) {
                            _flow.update {
                                if (it is DataState.Ok && !it.isCache){
                                    it
                                } else {
                                    DataState.Companion.ok(d, true, timestamp = file.lastModified())
                                }
                            }
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    override suspend fun update(data: T, isCache: Boolean, timestamp: Long) {
        val data = DataState.Companion.ok(data, isCache, timestamp = timestamp)
        _flow.update {
            data
        }
        if (!isCache) {
            // 异步缓存
            scope.launch {
                try {
                    val tempFile = tempFile ?: return@launch
                    val file = file ?: return@launch
                    val folder = folder ?: return@launch

                    if (!folder.exists()) {
                        folder.createDirectory()
                    }
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                    tempFile.openSink(false).buffer().use {
                        save(data.data, it)
                        it.flush()
                    }
                    if (file.exists()) {
                        file.delete()
                    }
                    tempFile.renameTo(name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }


    }

    private fun innerRefresh() {
        scope.launch {
            _flow.update {
                DataState.Companion.loading(cacheData = it.okOrNull())
            }
            val remote = fetchRemoteData()
//            logger.info("remote data state: $remote")
            when (remote) {
                is DataState.Ok -> {
//                    logger.info("remote data state update: $remote")
                    _flow.update {
                        remote
                    }
                    logger.info(_flow.value.toString())
                    // 异步缓存
                    scope.launch {
                        try {
                            val tempFile = tempFile ?: return@launch
                            val file = file ?: return@launch
                            val folder = folder ?: return@launch

                            if (!folder.exists()) {
                                folder.createDirectory()
                            }
                            if (tempFile.exists()) {
                                tempFile.delete()
                            }
                            tempFile.openSink(false).buffer().use {
                                save(remote.data, it)
                                it.flush()
                            }
                            if (file.exists()) {
                                file.delete()
                            }
                            tempFile.renameTo(name)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                is DataState.Error -> {
                    _flow.update {
                        DataState.error(
                            errorMsg = remote.errorMsg,
                            throwable = remote.throwable,
                            dataCache = it.cacheData
                        )
                    }
                }
                else -> {

                }
            }
        }
    }

}