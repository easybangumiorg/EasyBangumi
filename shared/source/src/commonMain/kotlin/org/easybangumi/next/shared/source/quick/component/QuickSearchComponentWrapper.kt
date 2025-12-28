package org.easybangumi.next.shared.source.quick.component

import com.dokar.quickjs.QuickJs
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.search.ISearchComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.quick.utils.callFunctionWithDataState
import org.easybangumi.next.shared.source.quick.utils.checkFunctionExists
import org.easybangumi.next.shared.source.quick.utils.toDataState
import kotlin.reflect.KClass

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
class QuickSearchComponentWrapper(
    searchComponent: ISearchComponent
): QuickComponentWrapper,
    BaseComponent(),
    SearchComponent,
    ISearchComponent by searchComponent {

    companion object {
        const val COMPONENT_NAME_SEARCH = "SearchComponent"
        const val FUNCTION_NAME_SEARCH = "search"
    }

    class Factory: QuickComponentWrapper.Factory<QuickSearchComponentWrapper> {

        override suspend fun create(quickJs: QuickJs): QuickSearchComponentWrapper? {
            // 1. check functions
            if (!quickJs.checkFunctionExists(
                "${COMPONENT_NAME_SEARCH}_${FUNCTION_NAME_SEARCH}",
            )) {
                return null
            }

            // 2. create proxy
            val searchComponentProxy: ISearchComponent = object: ISearchComponent {
                // js 源默认空字符串，业务自己转换
                override fun firstKey(): String {
                    return ""
                }

                override suspend fun search(
                    keyword: String,
                    key: String
                ): DataState<PagingFrame<CartoonCover>> {
                    return quickJs.callFunctionWithDataState("${COMPONENT_NAME_SEARCH}_${FUNCTION_NAME_SEARCH}", args = arrayOf(keyword, key)).toDataState<PagingFrame<CartoonCover>>()
                        ?: DataState.Error("QuickJS SearchComponent search returned invalid data: null")
                }
            }

            // 3. return wrapper
            return QuickSearchComponentWrapper(
                searchComponent = searchComponentProxy
            )
        }

    }

    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(SearchComponent::class)
    }
}