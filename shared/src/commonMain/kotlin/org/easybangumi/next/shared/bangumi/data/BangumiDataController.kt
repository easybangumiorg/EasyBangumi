package org.easybangumi.next.shared.bangumi.data

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.bangumi.data.repository.BangumiCharacterListRepository
import org.easybangumi.next.shared.bangumi.data.repository.BangumiCollectionRepository
import org.easybangumi.next.shared.bangumi.data.repository.BangumiPersonListRepository
import org.easybangumi.next.shared.bangumi.data.repository.BangumiSubjectRepository
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.case.DetailSourceCase

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
class BangumiDataController(
    private val detailSourceCase: DetailSourceCase,
    private val bangumiAccountController: BangumiAccountController,
) {

    private val bangumiRootFileUfd = pathProvider.getFilePath("bangumi")
    private val bangumiFileFolderUfd = bangumiRootFileUfd.child("info")
    private val bangumiUserFileUfd = bangumiRootFileUfd.child("user")
    private val collectionRepositoryMap = hashMapOf<String, BangumiCollectionRepository>()
    private val subjectRepositoryMap = hashMapOf<String, BangumiSubjectRepository>()
    private val personListRepositoryMap = hashMapOf<String, BangumiPersonListRepository>()
    private val characterListRepositoryMap = hashMapOf<String, BangumiCharacterListRepository>()

    val detailBusiness by lazy {
        detailSourceCase.getBangumiDetailBusiness()
    }

    val collectBusiness by lazy {
        detailSourceCase.getBangumiCollectBusiness()
    }


    private val lock: ReentrantLock = reentrantLock()
    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)


    init {
        scope.launch {
            bangumiAccountController.flow.collectLatest { info ->
                if (info != BangumiAccountController.BangumiAccountInfo.EMPTY) {
                    lock.withLock {
                        collectionRepositoryMap.forEach {
                            it.value.updateAccount(info)
                        }
                    }
                }
            }
        }
    }

    fun getSubjectRepository(cartoonIndex: CartoonIndex): BangumiSubjectRepository {
        // 先尝试不加锁获取
        val temp = subjectRepositoryMap[cartoonIndex.id]
        if (temp != null) {
            return temp
        }
        lock.withLock {
            return subjectRepositoryMap.getOrPut(cartoonIndex.id) {
                BangumiSubjectRepository(
                    folder = getSubjectFolder(cartoonIndex),
                    cartoonIndex = cartoonIndex,
                    bangumiDetailBusiness = detailBusiness,
                    scope
                )
            }
        }
    }

    fun getPersonListRepository(cartoonIndex: CartoonIndex): BangumiPersonListRepository {
        // 先尝试不加锁获取
        val temp = personListRepositoryMap[cartoonIndex.id]
        if (temp != null) {
            return temp
        }
        lock.withLock {
            return personListRepositoryMap.getOrPut(cartoonIndex.id) {
                BangumiPersonListRepository(
                    folder = getSubjectFolder(cartoonIndex),
                    cartoonIndex = cartoonIndex,
                    bangumiDetailBusiness = detailBusiness,
                    scope
                )
            }
        }
    }

    fun getCharacterListRepository(cartoonIndex: CartoonIndex): BangumiCharacterListRepository {
        // 先尝试不加锁获取
        val temp = characterListRepositoryMap[cartoonIndex.id]
        if (temp != null) {
            return temp
        }
        lock.withLock {
            return characterListRepositoryMap.getOrPut(cartoonIndex.id) {
                BangumiCharacterListRepository(
                    folder = getSubjectFolder(cartoonIndex),
                    cartoonIndex = cartoonIndex,
                    bangumiDetailBusiness = detailBusiness,
                    scope
                )
            }
        }
    }

    fun getCollectRepository(cartoonIndex: CartoonIndex): BangumiCollectionRepository? {
        val accountInfo = bangumiAccountController.flow.value
        if (accountInfo == BangumiAccountController.BangumiAccountInfo.EMPTY) {
            return null
        }
        return getCollectRepository(accountInfo, cartoonIndex)
    }

    private fun getCollectRepository(accountInfo: BangumiAccountController.BangumiAccountInfo, cartoonIndex: CartoonIndex): BangumiCollectionRepository? {
        val temp = collectionRepositoryMap[cartoonIndex.id]
        if (temp != null) {
            temp.updateAccount(accountInfo)
            return temp
        }
        lock.withLock {
            return collectionRepositoryMap.getOrPut(cartoonIndex.id) {
                BangumiCollectionRepository(
                    folder = bangumiUserFileUfd ?: throw IllegalStateException("bangumi getUserFile null"),
                    accountInfo = accountInfo,
                    bangumiCollectBusiness = collectBusiness,
                    cartoonIndex = cartoonIndex,
                    scope = scope,
                )
            }
        }
    }


    private fun getSubjectFolder(cartoonIndex: CartoonIndex): UFD {
        return bangumiFileFolderUfd?.child(cartoonIndex.id) ?: throw IllegalStateException("bangumi getSubjectFolder null")
    }





}