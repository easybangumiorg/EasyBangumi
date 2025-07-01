package org.easybangumi.next.shared.plugin.core.source

import org.easybangumi.next.shared.plugin.api.inner.InnerSource
import org.easybangumi.next.shared.plugin.debug.DebugSource
import org.easybangumi.shared.plugin.bangumi.plugin.BangumiInnerSource

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
object InnerSourceProvider {
    val InnerSourceLists: List<InnerSource> by lazy {
        listOf<InnerSource>(
            BangumiInnerSource(),
            DebugSource(),
        )
    }
}