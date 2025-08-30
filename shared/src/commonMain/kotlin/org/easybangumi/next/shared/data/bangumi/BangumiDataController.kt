package org.easybangumi.next.shared.data.bangumi

import com.mayakapps.kache.ContainerKache
import com.mayakapps.kache.KeyTransformer
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.encodeUtf8
import org.easybangumi.next.lib.store.cache.FileKache
import org.easybangumi.next.lib.utils.CoroutineProvider
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider
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
    private val detailSourceCase: DetailSourceCase
) {

    val detailComponent by lazy {
        detailSourceCase.getBangumiDetailBusiness()
    }

    private val lock: ReentrantLock = reentrantLock()
    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val keyTransformer = object: KeyTransformer {
        override suspend fun transform(oldKey: String): String {
            return oldKey.encodeUtf8().utf8()
        }
    }

    private val subjectKache: ContainerKache<String, String>? by lazy {
        runBlocking {
            FileKache(pathProvider.getCachePath("bangumi_subject"), 3 * 1024 * 1024) {
                keyTransformer = this@BangumiDataController.keyTransformer
            }
        }
    }


    private val personListKache: ContainerKache<String, String>? by lazy {
        runBlocking {
            FileKache(pathProvider.getCachePath("bangumi_person_list"), 1 * 1024 * 1024) {
                keyTransformer = this@BangumiDataController.keyTransformer
            }
        }
    }
    private val characterListKache: ContainerKache<String, String>? by lazy {
        runBlocking {
            FileKache(pathProvider.getCachePath("bangumi_character_list"), 1 * 1024 * 1024){
                keyTransformer = this@BangumiDataController.keyTransformer
            }
        }
    }

    private val subjectRepositoryMap = hashMapOf<String, BangumiSubjectRepository>()
    private val personListRepositoryMap = hashMapOf<String, BangumiPersonListRepository>()
    private val characterListRepositoryMap = hashMapOf<String, BangumiCharacterListRepository>()


    fun getSubjectRepository(cartoonIndex: CartoonIndex): BangumiSubjectRepository {
        // 先尝试不加锁获取
        val temp = subjectRepositoryMap[cartoonIndex.id]
        if (temp != null) {
            return temp
        }
        lock.withLock {
            return subjectRepositoryMap.getOrPut(cartoonIndex.id) {
                BangumiSubjectRepository(
                    cartoonIndex = cartoonIndex,
                    bangumiDetailBusiness = detailComponent,
                    subjectKache = subjectKache,
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
                    cartoonIndex = cartoonIndex,
                    bangumiDetailBusiness = detailComponent,
                    subjectKache = personListKache,
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
                    cartoonIndex = cartoonIndex,
                    bangumiDetailBusiness = detailComponent,
                    subjectKache = characterListKache,
                    scope
                )
            }
        }
    }




}