package org.easybangumi.next.shared.bangumi.data.repository

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
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.store.repository.FileMutableAbsRepository
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.MutableDataRepository
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.bangumi.BgmSubject
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.bangumi.source.BangumiCollectComponent

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
class BangumiCollectionRepository(
    folder: UFD,
    private var accountInfo: BangumiAccountController.BangumiAccountInfo,
    val bangumiCollectBusiness: ComponentBusiness<BangumiCollectComponent>,
    private val cartoonIndex: CartoonIndex,
    private val scope: CoroutineScope,
): MutableDataRepository<BgmCollect> {

    protected val logger = logger(this.toString())

    private val _flow = MutableStateFlow<DataState<BgmCollect>>(DataState.Companion.none())
    override val flow: StateFlow<DataState<BgmCollect>> = _flow

    private val folder by lazy {
        UniFileFactory.fromUFD(folder)
    }

    private var file =  this.folder?.child(accountInfo.username)?.child(cartoonIndex.id + ".json")
    private var tempFile= this.folder?.child(accountInfo.username)?.child(cartoonIndex.id + ".json.temp")


    override fun refresh(): Boolean {
        innerRefresh()
        return true
    }
    suspend fun fetchRemoteData(): DataState<BgmCollect> {
        return bangumiCollectBusiness.run {
            getCollection(accountInfo.username, accountInfo.token, cartoonIndex)
        }
    }

    fun save(data: BgmCollect, sink: BufferedSink) {
        val json = jsonSerializer.serialize(data)
        logger.info(json)
        if (json.isNotEmpty()) {
            sink.writeUtf8(json)
        }
    }
    fun load(source: BufferedSource): BgmCollect? {
        val text = source.readUtf8()
        return jsonSerializer.deserialize(text, BgmCollect::class, null)
    }


    init {
        innerLoadCache()
    }

    override suspend fun update(data: BgmCollect, isCache: Boolean, timestamp: Long) {
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
                    tempFile.renameTo(cartoonIndex.id + ".json")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }


    }

    private fun innerLoadCache() {
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
    private fun innerRefresh() {
        scope.launch {
            if (accountInfo == BangumiAccountController.BangumiAccountInfo.EMPTY) {
                _flow.update {
                    DataState.error("bangumi not token")
                }
                return@launch
            }
            _flow.update {
                DataState.Companion.loading(cacheData = it.okOrNull())
            }
            val un = accountInfo.username
            val remote = fetchRemoteData()
//            logger.info("remote data state: $remote")
            when (remote) {
                is DataState.Ok -> {
                    if (un != accountInfo.username) {
                        return@launch
                    }
//                    logger.info("remote data state update: $remote")
                    _flow.update {
                        remote
                    }
                    if (un != accountInfo.username) {
                        return@launch
                    }
                    logger.info(_flow.value.toString())
                    // 异步缓存
                    scope.launch {
                        if (un != accountInfo.username) {
                            return@launch
                        }
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
                            tempFile.renameTo(cartoonIndex.id + ".json")
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

//    fun updateAccount(accountInfo: BangumiAccountController.BangumiAccountInfo) {
//        if (this.accountInfo == accountInfo) {
//            return
//        }
//        _flow.update { DataState.None() }
//        this.accountInfo = accountInfo
//        file =  this.folder?.child(accountInfo.username)?.child(cartoonIndex.id + ".json")
//        tempFile= this.folder?.child(accountInfo.username)?.child(cartoonIndex.id + ".json.temp")
//        // 先加载缓存后拉数据
//        innerLoadCache()
//        refresh()
//    }


}