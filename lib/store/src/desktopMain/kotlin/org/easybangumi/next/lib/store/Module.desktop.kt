package org.easybangumi.next.lib.store

import org.easybangumi.next.lib.store.preference.JournalMapPreferenceStore
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.utils.pathProvider
import org.koin.core.module.Module
import org.koin.dsl.bind

actual fun Module.preferenceStore() {
    single {
        val journalMapHelper = JournalMapHelper(
            pathProvider.getFilePath("preference"),
            "config"
        )
        JournalMapPreferenceStore(journalMapHelper) as PreferenceStore
    }.bind<PreferenceStore>().bind(PreferenceStore::class)
}