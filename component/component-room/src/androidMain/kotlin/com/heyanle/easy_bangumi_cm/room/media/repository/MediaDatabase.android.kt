package com.heyanle.easy_bangumi_cm.room.media.repository

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module


actual val mediaDatabaseModule: Module
    get() = module {
        single {
            val context: Context = get()
            val appContext = context.applicationContext
            val dbFile = appContext.getDatabasePath(MediaDatabase.DB_FILE_NAME)
            Room.databaseBuilder<MediaDatabase>(
                context = appContext,
                name = dbFile.absolutePath
            ).setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .addMigrations(* mediaDatabaseMigrateList)
                .build()
        }
    }