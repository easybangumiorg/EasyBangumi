package com.heyanle.easybangumi4.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heyanle.easybangumi4.DB

/**
 * Created by HeYanLe on 2023/1/17 0:26.
 * https://github.com/heyanLE
 */
@Database(
    entities = [

    ],
    // 历史遗留问题
    autoMigrations = [],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        fun init(context: Context) {
            DB = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "easy_bangumi"
            ).fallbackToDestructiveMigration()
                .apply {

                }.build()
        }
    }

}