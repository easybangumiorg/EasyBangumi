package org.easybangumi.next.shared.plugin.utils

import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.api.utils.PreferenceHelper
import org.easybangumi.next.shared.plugin.utils.core.PreferenceHelperImpl
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.reflect.KClass

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
val utilsModule = module {
    single {
        PreferenceHelperImpl(it.get())
    }.bind<PreferenceHelper>()
}