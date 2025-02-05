package com.heyanle.easy_bangumi_cm.repository.cartoon

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.heyanle.easy_bangumi_cm.base.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.model.system.ILogger
import com.heyanle.easy_bangumi_cm.repository.cartoon.database.CartoonDatabase
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.get
import kotlinx.coroutines.Dispatchers
import java.io.File

/**
 * Created by heyanlin on 2024/12/3.
 */
actual fun InjectScope.registerCartoonDatabase(): CartoonDatabase {
    val pathProvider: IPathProvider = get<IPathProvider>()
    val logger: ILogger = get<ILogger>()
    val dbFile = File(pathProvider.getFilePath("db"), CartoonDatabase.DB_FILE_NAME)
    logger.i("CartoonDatabase","desktop dbFile: ${dbFile.absolutePath}")
    return Room.databaseBuilder<CartoonDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(* Migrate.cartoonDatabaseMigration.toTypedArray())
         // .fallbackToDestructiveMigrationOnDowngrade(true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}