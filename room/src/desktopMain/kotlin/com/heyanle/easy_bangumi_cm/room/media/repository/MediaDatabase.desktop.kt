package com.heyanle.easy_bangumi_cm.room.media.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.heyanle.easy_bangumi_cm.room.media.repository.MediaDatabase
import kotlinx.coroutines.Dispatchers
import java.io.File

actual fun onCreateMediaDatabase(): MediaDatabase {
    val dbFile = File(System.getProperty("java.io.tmpdir"), MediaDatabase.DB_FILE_NAME)
    println(dbFile.absolutePath)
    return Room.databaseBuilder<MediaDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}