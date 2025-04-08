package org.easybangumi.next.lib.store

import org.easybangumi.next.lib.store.preference.AndroidPreferenceStore
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val storeModule: Module
    get() = module {

        // 1. PreferenceStore
        single {
            AndroidPreferenceStore(it.get())
        }.bind(PreferenceStore::class)

    }