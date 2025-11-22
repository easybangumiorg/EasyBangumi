package org.easybangumi.next.shared.cartoon

import org.easybangumi.next.shared.cartoon.collection.CartoonBangumiCollectionController
import org.easybangumi.next.shared.cartoon.collection.CartoonCollectionController
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
val cartoonModule = module {
    single {
        CartoonBangumiCollectionController(get())
    }

    single {
        CartoonCollectionController(get(), get())
    }
}