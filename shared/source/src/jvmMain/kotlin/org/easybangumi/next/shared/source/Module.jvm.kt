package org.easybangumi.next.shared.source

import org.easybangumi.next.shared.source.rhino.RhinoPluginLoaderFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val sourceModuleCore: Module
    get() = module {
        single {
            RhinoPluginLoaderFactory()
        }
    }