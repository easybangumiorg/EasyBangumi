package com.heyanle.easybangumi4.cartoon.repository.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.Migrate
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.SearchHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoV1
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonTagOld
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.OtherDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.SearchHistoryDao

/**
 * Created by heyanle on 2023/12/16.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonInfo::class,
        SearchHistory::class,

        // for migrate
        CartoonInfoV1::class,
        // for migrate
        CartoonTagOld::class,
    ],
    autoMigrations = [AutoMigration(1, 2)],
    version = 2,
    exportSchema = true
)
abstract class CartoonDatabase : RoomDatabase() {

    abstract fun cartoonInfoDao(): CartoonInfoDao
    val cartoonInfo: CartoonInfoDao by lazy { cartoonInfoDao() }

    abstract fun cartoonTagDao(): CartoonTagDao
    val cartoonTag: CartoonTagDao by lazy { cartoonTagDao() }


    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }


    abstract fun otherDao(): OtherDao
    val other: OtherDao by lazy { otherDao() }

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

        fun build(context: Context, file: String): CartoonDatabase{
            return Room.databaseBuilder(
                context,
                CartoonDatabase::class.java, file
            ).apply {
                Migrate.CartoonDB.getDBMigration().forEach {
                    addMigrations(it)
                }
            }.build()
        }
    }
}