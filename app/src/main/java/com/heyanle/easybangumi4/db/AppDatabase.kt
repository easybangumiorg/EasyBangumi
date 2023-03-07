package com.heyanle.easybangumi4.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.db.entity.CartoonHistory
import com.heyanle.easybangumi4.db.entity.CartoonStar

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonStar::class,
        CartoonHistory::class,
    ],
    autoMigrations = [],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {


    abstract fun cartoonStarDao(): CartoonStarDao
    val cartoonStar: CartoonStarDao by lazy { cartoonStarDao() }

    abstract fun cartoonHistoryDao(): CartoonHistoryDao
    val cartoonHistory: CartoonHistoryDao by lazy { cartoonHistoryDao() }

    companion object {
        fun init(context: Context) {
            DB = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "easy_cartoon"
            ).fallbackToDestructiveMigration()
                .apply {

                }.build()
        }
    }

}