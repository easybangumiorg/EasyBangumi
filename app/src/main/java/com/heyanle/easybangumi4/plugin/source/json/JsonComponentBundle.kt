package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.plugin.api.component.page.PageComponent
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.plugin.api.utils.api.RenderHelper
import com.heyanle.easybangumi4.plugin.source.bundle.ComponentBundle
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlin.reflect.KClass

class JsonComponentBundle(
    private val jsonSource: JsonSource,
) : ComponentBundle {

    private val bundle = linkedMapOf<KClass<*>, Any>()

    override suspend fun init() {
        val executor = JsonRuleExecutor(
            source = jsonSource,
            networkHelper = Inject.get<NetworkHelper>(jsonSource.key),
            okhttpHelper = Inject.get<OkhttpHelper>(jsonSource.key),
            renderHelper = Inject.get<RenderHelper>(jsonSource.key),
        )

        bundle[JsonSource::class] = jsonSource
        bundle[PageComponent::class] = JsonPageComponent(jsonSource, executor)
        if (jsonSource.rule.search != null) {
            bundle[SearchComponent::class] = JsonSearchComponent(jsonSource, executor)
        }
        if (jsonSource.rule.detail != null) {
            bundle[DetailedComponent::class] = JsonDetailedComponent(jsonSource, executor)
        }
        if (jsonSource.rule.play != null) {
            bundle[PlayComponent::class] = JsonPlayComponent(jsonSource, executor)
        }
    }

    override fun get(clazz: KClass<*>): Any? = bundle[clazz]

    override fun getComponentProxy(clazz: KClass<*>): Any? = get(clazz)

    override fun release() {
        bundle.clear()
    }
}
