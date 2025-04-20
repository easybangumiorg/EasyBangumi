package org.easybangumi.next.shared.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
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

    @Query("select * from CartoonInfo")
    fun flowAll(): Flow<List<CartoonInfo>>

}