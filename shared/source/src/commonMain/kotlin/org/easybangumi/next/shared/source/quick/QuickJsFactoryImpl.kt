package org.easybangumi.next.shared.source.quick

import com.dokar.quickjs.QuickJs
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.quickjs.QuickJsFactory

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
class QuickJsFactoryImpl: QuickJsFactory {

    override fun createQuickJs(): QuickJs {
        return QuickJs.create(coroutineProvider.io())
    }
}