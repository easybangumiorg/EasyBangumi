package org.easybangumi.next.shared.source.case

import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.collect.CollectComponent
import org.easybangumi.next.shared.source.api.component.detail.DetailComponent
import org.easybangumi.next.shared.source.bangumi.source.BangumiCollectComponent
import org.easybangumi.next.shared.source.bangumi.source.BangumiDetailComponent
import org.easybangumi.next.shared.source.core.inner.InnerSourceProvider

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

class DetailSourceCase(
    private val innerSourceProvider: InnerSourceProvider,
) {

    fun getBangumiDetailBusiness(): ComponentBusiness<BangumiDetailComponent> {
        return (innerSourceProvider.bangumiComponentBundle.getBusiness(
            DetailComponent::class,
        ) as? ComponentBusiness<BangumiDetailComponent>) ?: throw IllegalStateException("BangumiDetailComponent not found")
    }

    fun getBangumiCollectBusiness(): ComponentBusiness<BangumiCollectComponent> {
        return (innerSourceProvider.bangumiComponentBundle.getBusiness(
            CollectComponent::class,
        ) as? ComponentBusiness<BangumiCollectComponent>) ?: throw IllegalStateException("BangumiDetailComponent not found")
    }
}