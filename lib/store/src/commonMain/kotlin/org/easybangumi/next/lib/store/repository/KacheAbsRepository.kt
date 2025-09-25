package org.easybangumi.next.lib.store.repository

import com.mayakapps.kache.ContainerKache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.BufferedSink
import okio.BufferedSource
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.buffer
import okio.use
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.utils.DataRepository
import org.easybangumi.next.lib.utils.DataState

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
abstract class KacheAbsRepository<T: Any>(
    val cacheKey: String,
    val subjectKache: ContainerKache<String, String>?,
    val scope: CoroutineScope,
): DataRepository<T> {

    protected val logger = logger(this.toString()?:"KacheAbsRepository")

    abstract fun save(data: T, sink: BufferedSink)
    abstract fun load(source: BufferedSource): T?

    abstract suspend fun fetchRemoteData(): DataState<T>


    private val _flow = MutableStateFlow<DataState<T>>(DataState.Companion.none())
    override val flow: StateFlow<DataState<T>> = _flow

    override fun refresh(): Boolean {
        innerRefresh()
        return true
    }

    init {
        scope.launch {
            // 1. 先加载缓存
            val filePath = subjectKache?.getIfAvailable(cacheKey)
            if (filePath != null) {
                try {
                    val uni = UniFileFactory.formPath(filePath.toPath())
                    if (uni.exists() && uni.isFile() && uni.canRead()) {
                        uni.openSource().buffer().use {
                            val d = load(it)
                            logger.info("load cache data: $d")
                            if (d != null) {
                                _flow.update {
                                    // cache 优先级比网络数据低
                                    if (it is DataState.Ok && !it.isCache){
                                        it
                                    } else {
                                        DataState.Companion.ok(d, true, timestamp = uni.lastModified())
                                    }
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
                            subjectKache?.put(cacheKey) {
                                return@put try {
//                                    logger.info("cache data to kache: $cacheKey -> $it")
                                    val uni = UniFileFactory.formPath(it.toPath())
//                                    uni.getParentFile()?.createDirectory()
//                                    uni.delete()
                                    uni.openSink(false).buffer().use {
                                        save(remote.data, it)
                                        it.flush()
                                    }
                                    return@put uni.exists().apply {
                                        logger.info(this.toString())
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    false
                                }
                            }
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