package com.heyanle.easy_bangumi_cm.room.media.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

actual val mediaDatabaseModule: Module
    get() = module {
        single {
            val pathProvider: PathProvider = get<PathProvider>()
            val dbFile = File(pathProvider.getFilePath("db"), MediaDatabase.DB_FILE_NAME)
            println(dbFile.absolutePath)
            Room.databaseBuilder<MediaDatabase>(
                name = dbFile.absolutePath,
            ).setDriver(BundledSQLiteDriver())
                .addMigrations(* mediaDatabaseMigrateList)
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        } bind MediaDatabase::class
    }