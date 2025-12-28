package org.easybangumi.next.shared.bangumi.data

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.bangumi.account.BangumiAccountController
import org.easybangumi.next.shared.bangumi.data.repository.BangumiCharacterListRepository
import org.easybangumi.next.shared.bangumi.data.repository.BangumiPersonListRepository
import org.easybangumi.next.shared.bangumi.data.repository.BangumiSubjectRepository
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.SourceCase

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
    private val sourceCase: SourceCase,
    private val bangumiAccountController: BangumiAccountController,
) {

    private val bangumiRootFileUfd = pathProvider.getFilePath("bangumi")
    private val bangumiFileFolderUfd = bangumiRootFileUfd.child("info")

    private val subjectRepositoryMap = hashMapOf<String, BangumiSubjectRepository>()
    private val personListRepositoryMap = hashMapOf<String, BangumiPersonListRepository>()
    private val characterListRepositoryMap = hashMapOf<String, BangumiCharacterListRepository>()

    val detailBusiness by lazy {
        sourceCase.getBangumiDetailBusiness()
    }


    private val lock: ReentrantLock = reentrantLock()
    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    val userDataProviderFlow = bangumiAccountController.accountInfoFlow.map {
        if (it == BangumiAccountController.BangumiAccountInfo.EMPTY) {
            null
        } else {
            BangumiUserDataProvider(
                info = it,
                bangumiRootFileUfd = bangumiRootFileUfd,
                sourceCase = sourceCase,
                lock = lock,
                scope = scope,
            )
        }
    }.stateIn(scope, SharingStarted.Lazily, null)


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




    private fun getSubjectFolder(cartoonIndex: CartoonIndex): UFD {
        return bangumiFileFolderUfd?.child(cartoonIndex.id) ?: throw IllegalStateException("bangumi getSubjectFolder null")
    }





}