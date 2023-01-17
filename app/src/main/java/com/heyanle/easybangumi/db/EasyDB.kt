package com.heyanle.easybangumi.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi.db.dao.SearchHistoryDao
import com.heyanle.easybangumi.db.entity.SearchHistory

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [
        SearchHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    val searchHistory: SearchHistoryDao by lazy { searchHistoryDao() }
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