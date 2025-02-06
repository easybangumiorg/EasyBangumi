package com.heyanle.easy_bangumi_cm.database.cartoon

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonInfo
import com.heyanle.lib.inject.api.InjectScope

/**
 * Created by heyanlin on 2024/12/5.
 */

expect fun InjectScope.registerCartoonDatabase(): CartoonDatabase

@Database(entities = [CartoonInfo::class], version = 1)
@ConstructedBy(CartoonDatabaseConstructor::class)
abstract class CartoonDatabase : RoomDatabase() {

    companion object {
        const val DB_FILE_NAME = "cartoon.db"
    }

    abstract fun cartoonInfoDao(): CartoonInfoDao

}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object CartoonDatabaseConstructor : RoomDatabaseConstructor<CartoonDatabase>