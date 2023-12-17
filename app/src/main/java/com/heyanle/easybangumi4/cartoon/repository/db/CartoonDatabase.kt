package com.heyanle.easybangumi4.cartoon.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.Migrate
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.SearchHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.SearchHistoryDao

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonInfo::class,
        CartoonTag::class,
        SearchHistory::class,
    ],
    autoMigrations = [],
    version = 1,
    exportSchema = true
)
abstract class CartoonDatabase : RoomDatabase() {

    abstract fun cartoonInfoDao(): CartoonInfoDao
    val cartoonInfo: CartoonInfoDao by lazy { cartoonInfoDao() }

    abstract fun cartoonTagDao(): CartoonTagDao
    val cartoonTag: CartoonTagDao by lazy { cartoonTagDao() }
    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }


    companion object {
        fun build(context: Context): CartoonDatabase {
            return Room.databaseBuilder(
                context,
                CartoonDatabase::class.java, "easy_bangumi_cartoon"
            ).apply {
                Migrate.CartoonDB.getDBMigration().forEach {
                    addMigrations(it)
                }
            }.build()
        }
    }
}