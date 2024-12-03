package com.heyanle.easy_bangumi_cm.room

import com.heyanle.easy_bangumi_cm.room.media.repository.MediaDatabase
import com.heyanle.easy_bangumi_cm.room.media.repository.dao.MediaInfoDao
import com.heyanle.easy_bangumi_cm.room.media.repository.mediaDatabaseModule
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/12/1 16:03.
 * https://github.com/heyanLE
 */
val roomModule: Module
    get() = module {
        includes(mediaDatabaseModule)

        single {
            val mediaDatabase: MediaDatabase = get()
            mediaDatabase.mediaInfoDao()
        } bind MediaInfoDao::class
    }

