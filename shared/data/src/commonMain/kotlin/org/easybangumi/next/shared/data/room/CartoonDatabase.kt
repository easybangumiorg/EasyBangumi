package org.easybangumi.next.shared.data.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.room.dao.CartoonInfoDao

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

expect fun makeCartoonDatabase(): CartoonDatabase

@Database(entities = [CartoonInfo::class], version = 1)
@ConstructedBy(CartoonDatabaseConstructor::class)
abstract class CartoonDatabase : RoomDatabase() {

    companion object {
        const val DB_FILE_NAME = "cartoon.db"
    }

    abstract fun cartoonInfoDao(): CartoonInfoDao

}

@Suppress("KotlinNoActualForExpect")
expect object CartoonDatabaseConstructor : RoomDatabaseConstructor<CartoonDatabase> {
    override fun initialize(): CartoonDatabase
}