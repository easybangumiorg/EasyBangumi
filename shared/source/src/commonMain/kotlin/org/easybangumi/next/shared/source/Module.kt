package org.easybangumi.next.shared.source

import org.easybangumi.next.shared.source.case.DetailSourceCase
import org.easybangumi.next.shared.source.case.DiscoverSourceCase
import org.easybangumi.next.shared.source.core.inner.InnerSourceController
import org.koin.dsl.module
import kotlin.math.sin

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

val sourceModule = module {
    single {
        InnerSourceController()
    }
    single {
        DiscoverSourceCase(get())
    }
    single {
        DetailSourceCase(get())
    }
}