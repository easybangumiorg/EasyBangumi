package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.easy_bangumi_cm.shared.utils.MoshiArrayListJsonAdapter
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Created by heyanlin on 2024/12/3.
 */
class SharedModule: InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            addSingletonFactory {
                Moshi.Builder()
                    .add(MoshiArrayListJsonAdapter.FACTORY)
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
            }
        }
    }
}