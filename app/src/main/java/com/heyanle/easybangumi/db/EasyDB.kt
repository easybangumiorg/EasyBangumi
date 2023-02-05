package com.heyanle.easybangumi.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi.db.dao.BangumiHistoryDao
import com.heyanle.easybangumi.db.dao.BangumiStarDao
import com.heyanle.easybangumi.db.dao.SearchHistoryDao
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.db.entity.SearchHistory

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        SearchHistory::class,  // 搜索历史
        BangumiStar::class,    // 追番
        BangumiHistory::class, // 观看历史
    ],
    // 历史遗留问题
    autoMigrations = [
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }

    abstract fun bangumiStarDao(): BangumiStarDao
    val bangumiStar: BangumiStarDao by lazy { bangumiStarDao() }

    abstract fun bangumiHistoryDao(): BangumiHistoryDao
    val bangumiHistory: BangumiHistoryDao by lazy { bangumiHistoryDao() }
}

object EasyDB {
    lateinit var database: AppDatabase

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "easy_bangumi"
        ).fallbackToDestructiveMigration().build()
    }
}