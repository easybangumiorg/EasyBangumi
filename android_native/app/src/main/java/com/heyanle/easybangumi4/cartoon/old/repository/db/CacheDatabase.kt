package com.heyanle.easybangumi4.cartoon.old.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.Migrate
import com.heyanle.easybangumi4.cartoon.old.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.old.entity.CartoonInfoOld

/**
 * 用于缓存的数据库，可以管理大小和单独删除
 * Created by HeYanLe on 2023/8/13 17:24.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonInfoOld::class
    ],
    autoMigrations = [],
    version = 3,
    exportSchema = true
)
abstract class CacheDatabase : RoomDatabase() {

    abstract fun cartoonInfoDao(): CartoonInfoDao
    val cartoonInfo: CartoonInfoDao by lazy {cartoonInfoDao() }
    companion object {
        fun build(context: Context): CacheDatabase {
            return Room.databaseBuilder(
                context,
                CacheDatabase::class.java, "easy_cache"
            ).apply {
                fallbackToDestructiveMigration()
                Migrate.CacheDB.getDBMigration().forEach {
                    addMigrations(it)
                }
            }.build()
        }
    }
}