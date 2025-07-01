package org.easybangumi.next.shared.plugin.core.javascript.rhino.component.wrapper

import org.easybangumi.next.rhino.RhinoFunction
import org.easybangumi.next.rhino.RhinoScope
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.discover.BannerHeadline
import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.discover.RecommendTab
import org.easybangumi.next.shared.plugin.api.withResult
import org.easybangumi.next.shared.plugin.api.component.BaseComponent
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
class RhinoDiscoverComponentWrapper(
    val rhinoScope: RhinoScope,
    val bannerHeadline: BannerHeadline,
    val bannerFunction: RhinoFunction,
    val recommendTabFunction: RhinoFunction,
    val loadRecommendFunction: RhinoFunction,
) : DiscoverComponent, BaseComponent(), RhinoComponentWrapper {

    companion object {
        private const val COMPONENT_NAME_DISCOVER = "DiscoverComponent"
        private const val FUNCTION_NAME_BANNER_HEADLINE = "bannerHeadline"
        private const val FUNCTION_NAME_BANNER = "banner"
        private const val FUNCTION_NAME_RECOMMEND_TAB = "recommendTab"
        private const val FUNCTION_NAME_LOAD_RECOMMEND = "loadRecommend"
    }

    class Factory : RhinoWrapperFactory<RhinoDiscoverComponentWrapper> {

        override suspend fun create(rhinoScope: RhinoScope): RhinoDiscoverComponentWrapper? {
            val bannerHeadlineFunction = rhinoScope.findFunction("${COMPONENT_NAME_DISCOVER}_${FUNCTION_NAME_BANNER_HEADLINE}")
            val bannerFunction = rhinoScope.findFunction("${COMPONENT_NAME_DISCOVER}_${FUNCTION_NAME_BANNER}")
            val recommendTabFunction = rhinoScope.findFunction("${COMPONENT_NAME_DISCOVER}_${FUNCTION_NAME_RECOMMEND_TAB}")
            val loadRecommendFunction = rhinoScope.findFunction("${COMPONENT_NAME_DISCOVER}_${FUNCTION_NAME_LOAD_RECOMMEND}")

            if (bannerHeadlineFunction == null || bannerFunction == null || recommendTabFunction == null || loadRecommendFunction == null) {
                return null
            }

            val bannerHeadline = rhinoScope.callFunction<BannerHeadline>(bannerHeadlineFunction)

            return RhinoDiscoverComponentWrapper(
                rhinoScope,
                bannerHeadline,
                bannerFunction,
                recommendTabFunction,
                loadRecommendFunction
            )
        }
    }


    override fun bannerHeadline(): BannerHeadline {
        return bannerHeadline
    }

    override suspend fun banner(): SourceResult<List<CartoonCover>> {
        return withResult {
            rhinoScope.callFunction(bannerFunction)
        }
    }

    override suspend fun recommendTab(): SourceResult<List<RecommendTab>> {
        return withResult {
            rhinoScope.callFunction(recommendTabFunction)
        }
    }

    override suspend fun loadRecommend(
        tab: RecommendTab,
        key: String
    ): SourceResult<Pair<String?, List<CartoonCover>>> {
        return withResult {
            rhinoScope.callFunction(loadRecommendFunction, tab, key)
        }
    }

    override fun getComponentClazz(): Array<KClass<*>> {
        return arrayOf(DiscoverComponent::class)
    }
}