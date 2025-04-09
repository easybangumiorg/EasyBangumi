package org.easybangumi.next.lib.store

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Created by heyanlin on 2025/4/9.
 */
val storeModule: Module
    get() = module {
        preferenceStore()
    }

expect fun Module.preferenceStore()