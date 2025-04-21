package org.easybangumi.next.lib.store

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.easybangumi.next.lib.utils.coroutineProvider
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Created by heyanlin on 2025/4/9.
 */

val StoreScope: CoroutineScope by lazy {
    CoroutineScope(SupervisorJob() + CoroutineName("libstore") + coroutineProvider.io())
}

val StoreSingleDispatcher: CoroutineDispatcher by lazy {
    coroutineProvider.newSingle()
}

val storeModule: Module
    get() = module {
        preferenceStore()
    }

expect fun Module.preferenceStore()