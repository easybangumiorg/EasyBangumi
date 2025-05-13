package org.easybangumi.next.lib.store

import org.easybangumi.next.lib.store.preference.AndroidPreferenceStore
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.koin.core.module.Module
import org.koin.dsl.bind

actual fun Module.preferenceStore() {
    single {
        AndroidPreferenceStore(get()) as PreferenceStore
    }.bind<PreferenceStore>().bind(PreferenceStore::class)
}