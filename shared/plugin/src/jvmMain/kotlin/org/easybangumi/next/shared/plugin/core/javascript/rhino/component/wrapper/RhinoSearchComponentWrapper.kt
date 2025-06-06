package org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper

import org.easybangumi.next.rhino.RhinoFunction
import org.easybangumi.next.rhino.RhinoScope
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.SearchComponent
import org.easybangumi.next.shared.plugin.api.withResult
import org.easybangumi.next.shared.plugin.core.component.BaseComponent
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
class RhinoSearchComponentWrapper(
    private val rhinoScope: RhinoScope,
    private val firstKey: String,
    private val searchFunction: RhinoFunction
): SearchComponent, BaseComponent(), RhinoComponentWrapper {


    companion object {

        private const val COMPONENT_NAME_SEARCH = "SearchComponent"
        private const val FUNCTION_NAME_FIRST_KEY = "firstKey"
        private const val FUNCTION_NAME_SEARCH = "search"

    }

    class Factory: RhinoWrapperFactory<RhinoSearchComponentWrapper> {

        override suspend fun create(rhinoScope: RhinoScope): RhinoSearchComponentWrapper? {
            val firstKeyFunction = rhinoScope.findFunction("${COMPONENT_NAME_SEARCH}_${FUNCTION_NAME_FIRST_KEY}")
            val searchFunction = rhinoScope.findFunction("${COMPONENT_NAME_SEARCH}_${FUNCTION_NAME_SEARCH}")

            if (searchFunction == null) {
                return null
            }
            // 默认为 0
            val firstKey = firstKeyFunction?.let {
                rhinoScope.callFunction<String>(firstKeyFunction)
            }  ?: "0"

            return RhinoSearchComponentWrapper(
                rhinoScope,
                firstKey,
                searchFunction
            )
        }

    }


    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(SearchComponent::class)
    }

    override fun firstKey(): String {
        return firstKey
    }

    override suspend fun search(
        keyword: String,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>> {
        return withResult {
            rhinoScope.callFunction<Pair<String?, List<CartoonCover>>>(
                searchFunction,
                keyword,
                key
            )
        }
    }


}