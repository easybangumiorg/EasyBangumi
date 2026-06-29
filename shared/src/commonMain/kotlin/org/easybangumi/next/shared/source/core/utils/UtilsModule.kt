package org.easybangumi.next.shared.source.core.utils

import org.easybangumi.next.shared.source.api.utils.HttpHelper
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.koin.core.module.Module
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
expect val utilsModuleExpect: Module
internal val utilsModule = module {
    single {
        PreferenceHelperImpl(get())
    }.bind<PreferenceHelper>()
    single {
        NetworkHelperImpl()
    }.bind<NetworkHelper>()

    single {
        HttpHelperImpl(get(), get())
    }.bind<HttpHelper>()
    includes(utilsModuleExpect)
}