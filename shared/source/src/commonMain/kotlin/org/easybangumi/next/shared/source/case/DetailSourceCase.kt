package org.easybangumi.next.shared.source.case

import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.detail.DetailComponent
import org.easybangumi.next.shared.source.bangumi.source.BangumiDetailComponent
import org.easybangumi.next.shared.source.core.inner.InnerSourceController

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
    private val innerSourceController: InnerSourceController,
) {

    fun getBangumiDetailBusiness(): ComponentBusiness<BangumiDetailComponent> {
        return innerSourceController.bangumiSourceInfo.componentBundle.getBusiness(
            DetailComponent::class,
        ) as? ComponentBusiness<BangumiDetailComponent> ?: throw IllegalStateException("BangumiDetailComponent not found")
    }
}