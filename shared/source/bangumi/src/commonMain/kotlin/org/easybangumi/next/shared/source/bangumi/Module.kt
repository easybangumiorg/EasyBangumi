package org.easybangumi.next.shared.source.bangumi

import org.easybangumi.next.shared.ktor.ktorModule
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.business.BangumiBusiness
import org.koin.dsl.binds
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

internal val bangumiModule = module {
    single {
        BangumiBusiness(get())
    }
    single {
        get<BangumiBusiness>().api
    }.binds(arrayOf(BangumiApi::class))

    includes(ktorModule)
}