package com.heyanle.easy_bangumi_cm

import android.app.Application
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory

/**
 * Created by heyanlin on 2023/10/30.
 */
class BaseModule(
    private val application: Application,
): InjectModule {
    override fun InjectScope.registerInjectables() {

        addSingletonFactory {
            application
        }

    }
}