package com.heyanle.easybangumi4.case

import android.app.Application
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

/**
 * Created by heyanlin on 2023/10/30.
 */
class CaseModule(
    private val application: Application
) : InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            CartoonInfoCase(get())
        }
        addSingletonFactory {
            ExtensionCase(get())
        }
        addSingletonFactory {
            SourceStateCase(get())
        }
    }
}