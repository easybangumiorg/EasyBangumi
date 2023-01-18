package com.heyanle.easybangumi.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi.db.dao.BangumiStarDao
import com.heyanle.easybangumi.db.dao.SearchHistoryDao
import com.heyanle.easybangumi.db.entity.BangumiStar
import com.heyanle.easybangumi.db.entity.SearchHistory
import com.heyanle.lib_anim.entity.Bangumi
import com.heyanle.lib_anim.entity.BangumiDetail

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        SearchHistory::class ,  // 搜索历史
        BangumiStar::class ,    // 追番
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }

    abstract fun bangumiStarDao(): BangumiStarDao
    val bangumiStar: BangumiStarDao by lazy { bangumiStarDao() }
}
object EasyDB {
    lateinit var database:AppDatabase

    fun init(context: Context){
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "easy_bangumi"
        ).fallbackToDestructiveMigration().build()
    }
}