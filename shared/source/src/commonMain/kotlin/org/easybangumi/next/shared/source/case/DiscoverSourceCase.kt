package org.easybangumi.next.shared.source.case

import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.source.bangumi.source.BangumiDiscoverComponent
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

class DiscoverSourceCase (
    private val innerSourceProvider: InnerSourceProvider,
){

    fun getBangumiDiscoverBusiness(): ComponentBusiness<BangumiDiscoverComponent> {
        return innerSourceProvider.bangumiComponentBundle.getBusiness(DiscoverComponent::class) as? ComponentBusiness<BangumiDiscoverComponent>
            ?: throw IllegalStateException("BangumiDiscoverComponent not found")
    }

}