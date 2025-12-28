package org.easybangumi.next.shared.data.room.cartoon.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import org.easybangumi.next.shared.data.cartoon.CartoonInfo

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
@Dao
interface CartoonInfoDao {

    @Insert
    suspend fun insert(cartoonInfo: CartoonInfo)

    @Update
    suspend fun update(cartoonInfo: CartoonInfo)

    @Delete
    suspend fun delete(cartoonInfo: CartoonInfo)

    @Query("select * from CartoonInfo where CartoonInfo.fromId = :fromId and CartoonInfo.fromSourceKey = :fromSource limit 1")
    suspend fun findById(fromSource: String, fromId: String): CartoonInfo?

    @Query("select * from CartoonInfo where CartoonInfo.fromId = :fromId and CartoonInfo.fromSourceKey = :fromSource limit 1")
    fun flowById(fromSource: String, fromId: String): Flow<CartoonInfo?>

    @Query("select * from CartoonInfo")
    fun flowAll(): Flow<List<CartoonInfo>>

    @Query("select * from CartoonInfo where CartoonInfo.lastHistoryTime > 0 order by CartoonInfo.lastHistoryTime desc limit :limit")
    fun flowHistory(limit: Int): Flow<List<CartoonInfo>>

    @Query("select * from CartoonInfo where CartoonInfo.lastHistoryTime > 0 order by CartoonInfo.lastHistoryTime desc")
    fun flowHistory(): Flow<List<CartoonInfo>>

    @Query("select * from CartoonInfo where CartoonInfo.starTime > 0")
    fun flowCollectionLocal(): Flow<List<CartoonInfo>>

    @Transaction
    suspend fun transaction(block: suspend () -> Unit) {
        block()
    }

    @Transaction
    suspend fun modify(cartoonInfo: CartoonInfo) {
        val old = findById(cartoonInfo.fromSourceKey, cartoonInfo.fromId)
        if (old == null) {
            insert(cartoonInfo.copy(createTime = Clock.System.now().toEpochMilliseconds()))
        } else {
            update(cartoonInfo.copy(createTime = old.createTime))
        }
    }

    @Transaction
    suspend fun modify(cartoonInfo: List<CartoonInfo>) {
        cartoonInfo.forEach {
            modify(it)
        }
    }






}