package com.heyanle.easy_bangumi_cm.business.media

import MediaDatabase
import androidx.room.Room
import com.heyanle.easy_bangumi_cm.component.provider.path.PathProvider
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val MODULE_PLATFORM_BUSINESS_MEDIA: Module
    get() = module {

        // Database Builder
        factory {
            val pathProvider = it.get<PathProvider>()
            val dbFile = pathProvider.getFilePath(MediaDatabase.DB_FILE_NAME)
            return@factory Room.databaseBuilder<MediaDatabase>(
                name = dbFile,
            )
        }

    }