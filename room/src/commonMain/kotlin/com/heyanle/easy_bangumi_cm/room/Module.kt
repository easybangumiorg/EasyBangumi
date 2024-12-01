package com.heyanle.easy_bangumi_cm.room

import com.heyanle.easy_bangumi_cm.room.media.mediaModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/12/1 16:03.
 * https://github.com/heyanLE
 */
val roomModule: Module
    get() = module {
        includes(mediaModule)
    }