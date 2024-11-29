package com.heyanle.easy_bangumi_cm.media.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

actual fun onCreateMediaDatabase(): MediaDatabase {
     val dbFile = File(System.getProperty("java.io.tmpdir"), MediaDatabase.DB_FILE_NAME)
    return Room.databaseBuilder<MediaDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}