package com.heyanle.easy_bangumi_cm.media

import com.heyanle.easy_bangumi_cm.media.repository.MediaDatabase
import com.heyanle.easy_bangumi_cm.media.repository.getMediaDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/28 22:21.
 * https://github.com/heyanLE
 */

fun initMediaModule(){
    startKoin {
        module {

            single {
                getMediaDatabase()
            }

            single {
                it.get<MediaDatabase>().mediaInfoDao()
            }
        }
    }
}