package org.easybangumi.next.shared.plugin

import org.easybangumi.next.shared.plugin.core.extension.ExtensionController
import org.easybangumi.next.shared.plugin.core.source.SourceConfigController
import org.easybangumi.next.shared.plugin.core.source.SourceController
import org.koin.core.module.dsl.singleOf
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

val pluginModule get() = module {

    single {
        ExtensionController()
    }

    single {
        SourceConfigController()
    }

    single {
        SourceController(get(), get())
    }

}