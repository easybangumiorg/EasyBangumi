package org.easybangumi.next.shared.plugin.component.meta

import org.easybangumi.next.shared.plugin.component.ComponentBundle
import org.easybangumi.next.shared.plugin.component.PlayComponent
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.SourceResult
import org.easybangumi.next.shared.plugin.component.MetaComponent


/**
 * Created by HeYanLe on 2024/12/8 21:38.
 * https://github.com/heyanLE
 */

interface SearchComponent : MetaComponent {

    suspend fun firstKey(keyword: String): String

    suspend fun search(keyword: String, searchKey: String): SourceResult<Pair<String?, List<CartoonCover>>>
}

fun ComponentBundle.searchComponent(): SearchComponent? {
    return this.getComponent(SearchComponent::class)
}