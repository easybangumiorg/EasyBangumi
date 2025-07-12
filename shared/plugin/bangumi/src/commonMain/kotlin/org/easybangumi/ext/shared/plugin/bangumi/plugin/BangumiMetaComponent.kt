package org.easybangumi.ext.shared.plugin.bangumi.plugin

import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.plugin.api.component.BaseComponent
import org.easybangumi.next.shared.plugin.api.component.meta.MetaComponent
import org.koin.core.component.inject

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

class BangumiMetaComponent: MetaComponent<BangumiMetaManager>, BaseComponent() {

    private val api: BangumiApi by inject()
    private val innerMateManager: BangumiMetaManager by lazy {
        BangumiMetaManager(api)
    }

    override fun getMateManager(): BangumiMetaManager {
        return innerMateManager
    }

}