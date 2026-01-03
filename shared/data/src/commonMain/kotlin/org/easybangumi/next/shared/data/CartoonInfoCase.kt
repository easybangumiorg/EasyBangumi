package org.easybangumi.next.shared.data

import kotlinx.datetime.Clock
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.CartoonTag
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

    // 必须为 cover 级别的数据才能收藏
    suspend fun changeCartoonInfoTag(
        cartoonCover: CartoonCover,
        newTag: Set<CartoonTag>?,
    ) {
        cartoonInfoDao.transaction {
            val info = cartoonInfoDao.findById(cartoonCover.source, cartoonCover.id, )
            if (info != null) {
                if (newTag?.isNotEmpty() != true) {
                    val newInfo = info.copy(
                        starTime = 0L,
                        tagsIdListString = "",
                    )
                    cartoonInfoDao.modify(newInfo)
                    return@transaction
                }
                val newInfo = info.copy(
                    starTime = if (info.starTime > 0L) info.starTime else Clock.System.now().toEpochMilliseconds(),
                    tagsIdListString = newTag.joinToString(",") { it.label },
                )
                cartoonInfoDao.modify(newInfo)
            } else if (newTag?.isNotEmpty() == true) {
                val newInfo = CartoonInfo.fromCartoonCover(cartoonCover)
                    .copy(
                        starTime = Clock.System.now().toEpochMilliseconds(),
                        tagsIdListString = newTag.joinToString(",") { it.label },
                    )
                cartoonInfoDao.modify(newInfo)
            }
        }

    }


}