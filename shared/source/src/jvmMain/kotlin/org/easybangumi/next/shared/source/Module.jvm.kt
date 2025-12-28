package org.easybangumi.next.shared.source

import org.easybangumi.next.shared.source.plugin.PluginLoader
import org.easybangumi.next.shared.source.rhino.RhinoPluginLoaderFactory
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

actual val sourceModuleCore: Module
    get() = module {

    }