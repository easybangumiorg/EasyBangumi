package org.easybangumi.next.shared.source.api.component.search

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.Component

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

interface ISearchComponent {

    fun firstKey(): String

    suspend fun search(
        keyword: String,
        key: String,
    ): DataState<PagingFrame<CartoonCover>>

    suspend fun searchWithCheck(
        keyword: String,
        key: String,
        web: IWebView
    ): DataState<PagingFrame<CartoonCover>> {
        return DataState.error("unsupported search with check")
    }

}

interface SearchComponent: Component, ISearchComponent