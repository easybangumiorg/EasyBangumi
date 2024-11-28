package com.heyanle.easy_bangumi_cm.media.repository

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.koin
import kotlinx.coroutines.Dispatchers
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

actual fun getMediaDatabase(): MediaDatabase {
    val ctx = koin.get<Context>()
    val name = MediaDatabase.DB_FILE_NAME
    return Room.databaseBuilder<MediaDatabase>(
        context = ctx,
        name = name,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}