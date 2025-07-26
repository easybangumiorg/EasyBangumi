package org.easybangumi.next.shared.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.source.case.DetailSourceCase
import org.easybangumi.next.shared.source.case.DiscoverSourceCase
import org.easybangumi.next.shared.source.case.PlaySourceCase
import org.easybangumi.next.shared.source.core.inner.InnerSourceProvider
import org.easybangumi.next.shared.source.core.source.SourceConfigController
import org.easybangumi.next.shared.source.core.source.SourceController
import org.easybangumi.next.shared.source.plugin.PluginSourceController
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
expect val sourceModuleCore: Module
val sourceModule = module {
    single {
        PlaySourceCase(get())
    }
    single {
        SourceConfigController()
    }
    single {
        SourceController(get(), get())
    }
    single {
        InnerSourceProvider(get())
    }
    single {
        PluginSourceController(CoroutineScope(SupervisorJob() + coroutineProvider.io()) , get(), get())
    }
    single {
        DiscoverSourceCase(get())
    }
    single {
        DetailSourceCase(get())
    }
    includes(sourceModuleCore)
}