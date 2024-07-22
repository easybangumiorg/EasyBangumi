package com.heyanle.easybangumi4.source.bundle

import android.app.Application
import android.content.Context
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source.SourceException
import com.heyanle.easybangumi4.source_api.IconSource
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.component.update.UpdateComponent
import com.heyanle.easybangumi4.source_api.utils.api.CaptchaHelper
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.extension_api.ExtensionIconSource
import com.heyanle.extension_api.ExtensionSource
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * 持有一个源的所有 Component 以及工具类
 * Created by HeYanLe on 2023/10/29 18:10.
 * https://github.com/heyanLE
 */
class ComponentBundle(
    private val source: Source
) {

    //其他允许注入的类
    private val contextClazz: Set<KClass<*>> = setOf(
        Context::class,
        Application::class,
    )

    // Source 接口
    private val sourceClazz: Set<KClass<*>> = setOf(
        ExtensionSource::class,
        IconSource::class,
        ExtensionIconSource::class,
        Source::class,

    )

    // 工具类接口
    private val utilsClazz: Set<KClass<*>> = setOf(
        StringHelper::class,
        NetworkHelper::class,
        OkhttpHelper::class,
        PreferenceHelper::class,
        WebViewHelper::class,
        WebViewHelperV2::class,
        CaptchaHelper::class
    )

    // Component 接口
    private val componentClazz: Set<KClass<*>> = setOf(
        PageComponent::class,
        DetailedComponent::class,
        SearchComponent::class,
        PreferenceComponent::class,
        UpdateComponent::class,
        PlayComponent::class
    )

    // 源自己注册的接口
    private val registerClazz: Set<KClass<*>> = source.register().toSet()

    private val bundle: HashMap<KClass<*>, Any> = hashMapOf()

    private val componentProxy:  HashMap<KClass<*>, Any> = hashMapOf()

    private val init = AtomicBoolean(false)


    // 工具注入
    init {
        put(StringHelper::class, Inject.get(source.key))
        put(NetworkHelper::class, Inject.get(source.key))
        put(OkhttpHelper::class, Inject.get(source.key))
        put(PreferenceHelper::class, Inject.get(source.key))
        put(WebViewHelper::class, Inject.get(source.key))
        put(CaptchaHelper::class, Inject.get(source.key))
        put(WebViewHelperV2::class, Inject.get(source.key))

        put(Context::class, APP)
        put(Application::class, APP)

        sourceClazz.forEach {
            if(it.isInstance(source)){
                putAnyway(it, source)
            }
        }

    }

    private fun putAnyway(clazz: KClass<*>, instance: Any){
        bundle[clazz] = instance
    }

    private fun <T : Any> put(clazz: KClass<T>, instance: T) {
        bundle[clazz] = instance
    }

    fun init() {
        if (init.compareAndSet(false, true)) {
            registerClazz.forEach {
                if (innerGet(it) == null) {
                    throw SourceException("Component 装配错误")
                }
            }
        }
    }

    private fun innerGet(clazz: KClass<*>, road: MutableSet<KClass<*>> = hashSetOf()): Any? {

        // source 本身可直接注入
        if (clazz.isInstance(source)) {
            return source
        }

        // 不允许注入除 工具，Component 以及 source 里 register 以外的类
        if (!utilsClazz.contains(clazz) &&
            !componentClazz.contains(clazz) &&
            !registerClazz.contains(clazz) &&
            !contextClazz.contains(clazz) &&
            clazz != ComponentWrapper::class
        ) {
            throw SourceException("尝试非法注入： ${clazz.simpleName}")
        }

        if (bundle.contains(clazz)) {
            return bundle[clazz]
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

        bundle[clazz] = instance
        componentClazz.forEach {
            if (it.isInstance(instance)) {
                bundle[it] = instance
            }
        }

        // ComponentWrapper 自动装配 source
        if (instance is ComponentWrapper) {
            instance.innerSource = source
        }

        return instance
    }

    fun get(clazz: KClass<*>): Any? {
        return bundle[clazz]
    }

    inline fun <reified T> get(): T? {
        return get(T::class) as? T
    }

    fun getComponentProxy(clazz: KClass<*>): Any? {
        val o = componentProxy[clazz]
        if (o == null){
            val instance = get(clazz) ?: return null
            if (!clazz.isInstance(instance) || instance !is Component){
                return null
            }
            val proxy = Proxy.newProxyInstance(instance.javaClass.classLoader, instance.javaClass.interfaces, ComponentProxy(instance))
            componentProxy[clazz] = proxy
            return proxy
        }
        return o
    }
    inline fun <reified T: Component> getComponentProxy(): T? {
        val obj = getComponentProxy(T::class)
        return obj as? T
    }

    fun release() {
        bundle.clear()
    }

}