package org.easybangumi.next.shared.source.api.component.search

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.ICacheComponent

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
class SearchComponentWrapper(
    private val innerComponent: SearchComponent,
): SearchComponent by innerComponent, ICacheComponent {

    private var lastKeyword: String? = null
    private val lastPagingFrameMap = mutableMapOf<String, PagingFrame<CartoonCover>>()


    override suspend fun search(keyword: String, key: String): DataState<PagingFrame<CartoonCover>> {
        if (keyword == lastKeyword) {
            val lastPagingFrame = lastPagingFrameMap[key]
            if (lastPagingFrame != null) {
                return DataState.ok(lastPagingFrame, isCache = true)
            }
        } else {
            lastKeyword = keyword
            lastPagingFrameMap.clear()
        }
        return innerComponent.search(keyword, key).apply {
            onOK {
                lastKeyword = keyword
                lastPagingFrameMap[key] = it
            }
        }
    }

    override fun clearCache() {
        lastKeyword = null
        lastPagingFrameMap.clear()
    }
}