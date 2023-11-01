package com.heyanle.easybangumi4.extension

import android.app.Application
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.extension_api.IconFactory
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addAlias
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/11/1.
 */
class ExtensionModule(
    private val application: Application
) : InjektModule {

    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            ExtensionController(application, application.getFilePath("extension"), application.getCachePath("extension"))
        }

        addSingletonFactory {
            ExtensionIconFactoryImpl(get())
        }
        addAlias<ExtensionIconFactoryImpl, IconFactory>()
    }
}