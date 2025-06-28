package org.easybangumi.next.shared.plugin.paging

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.component.filter.Filter
import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness

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
class CartoonFilterPagingSource(
    val filterList: List<Filter>,
    val filterBusiness: ComponentBusiness<FilterComponent>
): EasyPagingSource<CartoonCover> {

    override suspend fun load(key: String): DataState<PagingFrame<CartoonCover>> {
        return filterBusiness.run {
            search(filterList, key)
        }.toDataState()
    }

    override val initKey: String = filterBusiness.runDirect {
        firstKey(filterList)
    }

}