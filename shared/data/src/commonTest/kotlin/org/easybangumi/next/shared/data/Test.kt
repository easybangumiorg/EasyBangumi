package org.easybangumi.next.shared.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.room.CartoonDatabase
import org.easybangumi.next.shared.data.room.dao.CartoonInfoDao
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

class Test : KoinTest {

    @Test
    fun test() {

        startKoin {
            loadKoinModules(dataModule)
        }
        runBlocking {
            val cartoonInfoDao: CartoonInfoDao by inject ()

//            repeat(100) {
//                cartoonInfoDao.insert(
//                    newTestCartoonInfo(it)
//                )
//            }

            val cartoonInfoList = cartoonInfoDao.flowAll().first()
            assertEquals(100, cartoonInfoList.size)

            val fileFolder = pathProvider.getFilePath("database")
            val uni = UniFileFactory.fromUFD(fileFolder)?.delete()
        }

    }

    private fun newTestCartoonInfo(
        int: Int
    ) = CartoonInfo(
        "$int", "$int", "$int", "$int", "$int", false, "$int"
    )
}