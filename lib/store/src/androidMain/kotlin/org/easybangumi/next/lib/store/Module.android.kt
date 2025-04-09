package org.easybangumi.next.lib.store

import org.easybangumi.next.lib.store.preference.AndroidPreferenceStore
import org.koin.core.module.Module

actual fun Module.preferenceStore() {
    single {
        AndroidPreferenceStore(get())
    }
}