package org.easybangumi.next.shared.data.room

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.global
import org.easybangumi.next.lib.utils.pathProvider


actual fun makeCartoonDatabase(): CartoonDatabase {
    val fileFolder = pathProvider.getFilePath("database")
    val dbPath = UniFileFactory.fromUFD(fileFolder)?.child(CartoonDatabase.DB_FILE_NAME)?.getFilePath() ?: CartoonDatabase.DB_FILE_NAME
    val ctx = global.appContext
    return Room.databaseBuilder<CartoonDatabase>(
        context = ctx,
        name = dbPath,
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(* Migrate.cartoonDatabaseMigration.toTypedArray())
        // .fallbackToDestructiveMigrationOnDowngrade(true)
        .setQueryCoroutineContext(coroutineProvider.io())
        .build()
}