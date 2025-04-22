package org.easybangumi.next.shared.plugin.api.component.page

import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.Component

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
interface PageComponent : Component {

    suspend fun getCartoonPage(): SourceResult<List<CartoonPage>>

    suspend fun getPageTab(page: CartoonPage): SourceResult<List<PageTab>>

    suspend fun initKey(tab: PageTab): SourceResult<String>
    suspend fun loadPage(tab: PageTab, key: String, ): SourceResult<Pair<String?, List<CartoonPage>>>

}