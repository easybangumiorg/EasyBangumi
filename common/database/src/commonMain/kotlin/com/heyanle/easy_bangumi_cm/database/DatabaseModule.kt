package com.heyanle.easy_bangumi_cm.database

import com.heyanle.easy_bangumi_cm.database.cartoon.CartoonDatabase
import com.heyanle.easy_bangumi_cm.database.cartoon.CartoonInfoDao
import com.heyanle.easy_bangumi_cm.database.cartoon.registerCartoonDatabase
import com.heyanle.lib.inject.api.InjectModule
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.addSingletonFactory
import com.heyanle.lib.inject.api.get

/**
 * Created by heyanlin on 2024/12/3.
 */
class DatabaseModule : InjectModule {

    override fun InjectScope.registerInjectables() {

        // -- CartoonDatabase --
        addSingletonFactory<CartoonDatabase> {
            registerCartoonDatabase()
        }

        addSingletonFactory<CartoonInfoDao> {
            get<CartoonDatabase>().cartoonInfoDao()
        }
    }
}