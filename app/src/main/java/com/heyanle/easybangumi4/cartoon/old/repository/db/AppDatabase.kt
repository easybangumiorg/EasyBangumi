package com.heyanle.easybangumi4.cartoon.old.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.Migrate
import com.heyanle.easybangumi4.cartoon.old.repository.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.cartoon.old.repository.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.cartoon.old.repository.db.dao.CartoonTagDao
import com.heyanle.easybangumi4.cartoon.old.repository.db.dao.SearchHistoryDao
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.entity.CartoonTag
import com.heyanle.easybangumi4.cartoon.entity.SearchHistory
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonTagOld
import com.heyanle.easybangumi4.cartoon.old.entity.SearchHistoryOld

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonStar::class,
        CartoonHistory::class,
        SearchHistoryOld::class,
        CartoonTagOld::class,
    ],
    autoMigrations = [],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }

    abstract fun cartoonStarDao(): CartoonStarDao
    val cartoonStar: CartoonStarDao by lazy { cartoonStarDao() }

    abstract fun cartoonHistoryDao(): CartoonHistoryDao
    val cartoonHistory: CartoonHistoryDao by lazy { cartoonHistoryDao() }

    abstract fun cartoonTagDao(): CartoonTagDao
    val cartoonTag: CartoonTagDao by lazy { cartoonTagDao() }

    companion object {
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java, "easy_cartoon"
            ).apply {
                Migrate.AppDB.getDBMigration().forEach {
                    addMigrations(it)
                }
            }.build()
        }
    }

}