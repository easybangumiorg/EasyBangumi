package com.heyanle.easy_bangumi_cm.media

import com.heyanle.easy_bangumi_cm.koin
import com.heyanle.easy_bangumi_cm.media.repository.MediaDatabase
import com.heyanle.easy_bangumi_cm.media.repository.dao.MediaInfoDao
import com.heyanle.easy_bangumi_cm.media.repository.onCreateMediaDatabase
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/11/28 22:21.
 * https://github.com/heyanLE
 */
private val mediaModule = module {
    single {
        onCreateMediaDatabase()
    } bind MediaDatabase::class

    single {
        this.inject<MediaDatabase>().value.mediaInfoDao()
    } bind MediaInfoDao::class
}

fun initMediaModule() {
    println("init MediaModule")
    startKoin {
        modules(mediaModule)
    }
}