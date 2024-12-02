package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.easy_bangumi_cm.base.BaseFactory
import com.heyanle.easy_bangumi_cm.base.Logger
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.preference.PreferenceStore
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/12/3 0:17.
 * https://github.com/heyanLE
 */

object SharedApp {

    fun initBase(
        baseFactory: BaseFactory
    ){
        startKoin {
            modules(module {
                single {
                    baseFactory.getLogger()
                } bind Logger::class

                single {
                    baseFactory.getPathProvider()
                } bind PathProvider::class

                single {
                    baseFactory.getPreferenceStore()
                } bind PreferenceStore::class
            })
        }
    }


}