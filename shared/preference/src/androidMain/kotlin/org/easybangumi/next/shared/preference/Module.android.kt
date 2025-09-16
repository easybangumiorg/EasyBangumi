package org.easybangumi.next.shared.preference

import org.koin.core.module.Module
import org.koin.dsl.module

actual val expectModule: Module
    get() = module {
        single {
            AndroidPlayerPreference(get())
        }
    }