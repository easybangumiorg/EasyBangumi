package com.heyanle.easy_bangumi_cm.business.media

import MediaDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.math.sin

internal expect val MODULE_PLATFORM_BUSINESS_MEDIA: Module
val MODULE_BUSINESS_MEDIA: Module
    get() =  module {
        includes(MODULE_PLATFORM_BUSINESS_MEDIA)

        single {
            val dbBuilder = it.get<RoomDatabase.Builder<MediaDatabase>>()
            dbBuilder
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }

        single {
            val mediaDatabase: MediaDatabase = get()
            mediaDatabase.mediaInfoDao()
        }
    }
