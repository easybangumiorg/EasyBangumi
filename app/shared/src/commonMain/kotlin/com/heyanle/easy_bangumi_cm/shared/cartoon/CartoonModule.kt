package com.heyanle.easy_bangumi_cm.shared.cartoon

import com.heyanle.easy_bangumi_cm.shared.cartoon.database.CartoonDatabase
import com.heyanle.easy_bangumi_cm.shared.cartoon.database.dao.CartoonInfoDao
import com.heyanle.lib.inject.api.InjectModule
import com.heyanle.lib.inject.api.InjectScope
import com.heyanle.lib.inject.api.addSingletonFactory
import com.heyanle.lib.inject.api.get

/**
 * Created by heyanlin on 2024/12/3.
 */
expect fun InjectScope.registerCartoonDatabase(): CartoonDatabase

class CartoonModule : InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory<CartoonDatabase> {
            registerCartoonDatabase()
        }

        addSingletonFactory<CartoonInfoDao> {
            get<CartoonDatabase>().cartoonInfoDao()
        }
    }
}