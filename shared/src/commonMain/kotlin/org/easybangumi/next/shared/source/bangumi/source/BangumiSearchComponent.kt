package org.easybangumi.next.shared.source.bangumi.source

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.data.bangumi.toCartoonCover
import org.koin.core.component.inject
import kotlin.getValue

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
class BangumiSearchComponent: SearchComponent, BaseComponent() {

    private val api: BangumiApi by inject()

    override fun firstKey(): String {
        return "1"
    }

    override suspend fun search(
        keyword: String,
        key: String
    ): DataState<PagingFrame<CartoonCover>> {
        val page = key.toIntOrNull() ?: throw IllegalArgumentException("key is not a number")
        return (api.search(keyword, page))
            .await().toDataState().map {
                it.map {
                    it.toCartoonCover()
                }
            }.map {
                if (it.isEmpty()) {
                    null to it.filterNotNull()
                } else {
                    (page + 1).toString() to it.filterNotNull()
                }
            }
    }
}