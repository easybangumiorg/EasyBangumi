package com.heyanle.easybangumi4.cloudsync

import android.app.Application
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncCodec
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncMerger
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

class CloudSyncModule(
    private val application: Application,
) : InjectModule {
    override fun InjectScope.registerInjectables() {
        addSingletonFactory { CloudSyncPreferences(get<PreferenceStore>()) }
        addSingletonFactory { CloudSyncSecretStore(application) }
        addSingletonFactory { CloudSyncEventBatcher() }
        addSingletonFactory { CloudSyncLocalStore(application, get()) }
        addSingletonFactory { CloudSyncCodec(get()) }
        addSingletonFactory { CloudSyncMerger() }
        addSingletonFactory { OneDriveAuthController(application, get()) }
        addSingletonFactory {
            CloudSyncManager(
                cartoonRepository = get(),
                preferences = get(),
                secretStore = get(),
                eventBatcher = get(),
                localStore = get(),
                codec = get(),
                merger = get(),
                oneDriveAuthController = get(),
            )
        }
    }
}
