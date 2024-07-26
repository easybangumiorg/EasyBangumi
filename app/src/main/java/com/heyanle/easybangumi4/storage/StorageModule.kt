package com.heyanle.easybangumi4.storage

import android.app.Application
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

/**
 * Created by heyanlin on 2024/5/7.
 */
class StorageModule(
    private val application: Application
) : InjectModule {


    override fun InjectScope.registerInjectables() {

        addSingletonFactory {
            StoragePreference(get())
        }

        addSingletonFactory {
            BackupController(get(), get(), get(), get())
        }

        addSingletonFactory {
            RestoreController(get(), get(), get(), get(), get())
        }
    }
}