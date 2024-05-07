package com.heyanle.easybangumi4.storage

import android.app.Application
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2024/5/7.
 */
class StorageModule(
    private val application: Application
) : InjektModule {


    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            StorageController(get(), get(), get(), get(), get(), get())
        }
    }
}