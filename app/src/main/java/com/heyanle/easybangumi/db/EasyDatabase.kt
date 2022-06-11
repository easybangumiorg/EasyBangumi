package com.heyanle.easybangumi.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.db.dao.BangumiDao
import com.heyanle.easybangumi.db.dao.BangumiDetailDao
import com.heyanle.easybangumi.entity.BangumiDetail

/**
 * Created by HeYanLe on 2021/9/20 21:32.
 * https://github.com/heyanLE
 */
object EasyDatabase {



    val AppDB: AppDatabase by lazy {
        Room.databaseBuilder(
            EasyApplication.INSTANCE,
            AppDatabase :: class.java,
            "easy_bangumi"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }

}

@Database(entities = [BangumiDetail::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bangumiDetailDao(): BangumiDetailDao
    abstract fun bangumiDao(): BangumiDao
}