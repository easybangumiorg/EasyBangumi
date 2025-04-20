package org.easybangumi.next.shared.data.room

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.pathProvider

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

actual fun makeCartoonDatabase(): CartoonDatabase {
    val fileFolder = pathProvider.getFilePath("database")
    val dbPath = UniFileFactory.fromUFD(fileFolder)?.child(CartoonDatabase.DB_FILE_NAME)?.getFilePath() ?: CartoonDatabase.DB_FILE_NAME
    return Room.databaseBuilder<CartoonDatabase>(
        name = dbPath,
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(* Migrate.cartoonDatabaseMigration.toTypedArray())
        // .fallbackToDestructiveMigrationOnDowngrade(true)
        .setQueryCoroutineContext(coroutineProvider.io())
        .build()
}