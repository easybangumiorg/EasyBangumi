package com.heyanle.easybangumi4.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.db.dao.SearchHistoryDao
import com.heyanle.easybangumi4.db.entity.CartoonHistory
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.db.entity.SearchHistory

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        CartoonStar::class,
        CartoonHistory::class,
        SearchHistory::class,
    ],
    autoMigrations = [],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }

    abstract fun cartoonStarDao(): CartoonStarDao
    val cartoonStar: CartoonStarDao by lazy { cartoonStarDao() }

    abstract fun cartoonHistoryDao(): CartoonHistoryDao
    val cartoonHistory: CartoonHistoryDao by lazy { cartoonHistoryDao() }

    companion object {
        fun init(context: Context) {
            DB = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "easy_cartoon"
            ).apply {
                addMigrations(MIGRATION_2_3)
            }.build()
        }


        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN reversal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE CartoonStar ADD COLUMN watchProcess TEXT NOT NULL DEFAULT ''")
            }
        }
    }

}