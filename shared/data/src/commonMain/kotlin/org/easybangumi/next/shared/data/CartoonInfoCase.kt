package org.easybangumi.next.shared.data

import org.easybangumi.next.shared.data.room.cartoon.dao.CartoonInfoDao

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
class CartoonInfoCase(
    private val cartoonInfoDao: CartoonInfoDao,
) {

    fun flowCollectionLocal() = cartoonInfoDao.flowCollectionLocal()

    fun flowById(fromSource: String, fromId: String) = cartoonInfoDao.flowById(fromSource, fromId)

    fun flowHistory(limit: Int) = cartoonInfoDao.flowHistory(limit)

    fun flowHistory() = cartoonInfoDao.flowHistory()


}