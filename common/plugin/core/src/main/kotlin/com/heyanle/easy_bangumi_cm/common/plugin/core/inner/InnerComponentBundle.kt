package com.heyanle.easy_bangumi_cm.common.plugin.core.inner

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.component.ComponentHelper
import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceException
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.DetailedComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.MediaEventComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.PlayComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.SearchComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.PrefComponent
import com.heyanle.easy_bangumi_cm.plugin.api.source.MediaSource
import com.heyanle.easy_bangumi_cm.plugin.api.source.MetaSource
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.utils.PreferenceHelper
import com.heyanle.easy_bangumi_cm.plugin.utils.StringHelper
import com.heyanle.easy_bangumi_cm.plugin.utils.WebViewHelper
import com.heyanle.lib.inject.api.get
import com.heyanle.lib.inject.core.Inject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * Created by heyanlin on 2025/1/29.
 */
class InnerComponentBundle(
    private val innerSource: InnerSource
): ComponentBundle {

    companion object {

        // Source 里的接口
        val sourceClazz: Set<KClass<*>> = setOf(
            Source::class,
            MetaSource::class,
            MediaSource::class,
            InnerSource::class,
        )

        // 工具类接口
        val utilsClazz: Set<KClass<*>> = setOf(
            PreferenceHelper::class,
            WebViewHelper::class,
            StringHelper::class,
        )

        // Component 接口
        val componentClazz: Set<KClass<*>> = setOf(
            DetailedComponent::class,
            MediaEventComponent::class,
            PlayComponent::class,
            SearchComponent::class,
            HomeComponent::class,
            PrefComponent::class,
        )
    }

    private val registerClazz = innerSource.componentClazz.toSet()


    private val configProvider: EasyPluginConfigProvider by Inject.injectLazy()
    private val bundleMap: HashMap<KClass<*>, Any> = hashMapOf()
    private val componentMap: HashMap<KClass<*>, Component> = hashMapOf()
    private val init = AtomicBoolean(false)

    override fun getSource(): Source {
        return innerSource
    }

    override fun <T : Component> getComponent(clazz: KClass<T>): T? {
        return componentMap[clazz] as? T
    }

    suspend fun load() {
        if (init.compareAndSet(false, true)) {
            val source = getSource()
            put(StringHelper::class, Inject.get(source.key))
            put(PreferenceHelper::class, Inject.get(source.key))
            put(WebViewHelper::class, Inject.get(source.key))

            sourceClazz.forEach {
                if (it.isInstance(source)) {
                    putAnyway(it, source)
                }
            }
            registerClazz.forEach {
                if (innerGet(it) == null) {
                    throw SourceException("Component 装配错误")
                }
            }
        }
    }

    private fun putAnyway(clazz: KClass<*>, instance: Any) {
        bundleMap[clazz] = instance
    }

    private fun <T : Any> put(clazz: KClass<T>, instance: T) {
        bundleMap[clazz] = instance
    }

    private fun innerGet(clazz: KClass<*>, road: ArrayList<KClass<*>> = arrayListOf()): Any? {
        // source 本身可直接注入
        if (clazz.isInstance(getSource())) {
            return getSource()
        }

        // 不允许注入除 工具，Component 以及 source 里 register 以外的类
        if (!utilsClazz.contains(clazz) &&
            !componentClazz.contains(clazz) &&
            !registerClazz.contains(clazz) &&
            !sourceClazz.contains(clazz)
        ) {
            throw SourceException("尝试非法注入： ${clazz.simpleName}")
        }

        if (bundleMap.contains(clazz)) {
            return bundleMap[clazz]
        }

        // 循环依赖
        if (road.contains(clazz)) {
            throw SourceException("${clazz.simpleName} 存在循环依赖 ${road.map { it.simpleName }.joinToString(" ->")}")
        }

        val cons = clazz.constructors
        // 只支持一个构造方法
        if (cons.size != 1) {
            throw SourceException("${clazz.simpleName} 有多个构造方法")
        }

        val con = cons.first()
        val params = con.parameters
        val targetParams = arrayListOf<Any>()
        road.add(clazz)
        for (param in params) {
            // 只支持普通构造函数传参
            if (param.kind != KParameter.Kind.VALUE) {
                throw SourceException("${clazz.simpleName} 构造方法有特殊传参")
            }
            val kClazz = param.type.classifier as? KClass<*> ?: throw SourceException("${clazz.simpleName} 装配错误 1")
            // 构造出错
            val instance =
                innerGet(kClazz, road) ?: throw SourceException("${clazz.simpleName} 装配错误 2")
            targetParams.add(instance)
        }
        road.remove(clazz)
        val instance = kotlin.runCatching {
            con.call(*(targetParams.toArray()))
        }.getOrElse {
            it.printStackTrace()
            throw SourceException("${clazz.simpleName} 装配错误 ${it.message}")
        }

        // 不允许实现 utils 接口
        utilsClazz.forEach {
            if (it.isInstance(instance)) {
                throw SourceException("${clazz.simpleName} 实现了 utils 中的接口")
            }
        }

        // ComponentMap 里的可能是代理
        if (instance is Component) {
            val dComponent = decoComponent(instance)
            componentMap[clazz] = dComponent
            componentClazz.forEach {
                if (it.isInstance(dComponent)) {
                    componentMap[it] = dComponent
                }
            }
        }

        // BundleMap 里为原对象
        bundleMap[clazz] = instance
        componentClazz.forEach {
            if (it.isInstance(instance)) {
                bundleMap[it] = instance
            }
        }

        // 注入 innerSource
        if (instance is ComponentWrapper) {
            instance.innerSource = getSource()
        }

        return instance
    }

    private fun decoComponent(component: Component): Component {
        val proxy = configProvider.componentProxy
        return if (proxy != null) {
            ComponentHelper.getComponentProxy(component, proxy)
        } else {
            component
        }
    }
}