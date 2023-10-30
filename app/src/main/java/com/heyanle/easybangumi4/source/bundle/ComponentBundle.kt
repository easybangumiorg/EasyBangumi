package com.heyanle.easybangumi4.source.bundle

import androidx.collection.arraySetOf
import com.heyanle.easybangumi4.source.SourceException
import com.heyanle.easybangumi4.source.utils.StringHelperImpl
import com.heyanle.easybangumi4.source.utils.WebViewHelperImpl
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.component.update.UpdateComponent
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.api.OkhttpHelper
import com.heyanle.easybangumi4.source_api.utils.api.PreferenceHelper
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * Created by HeYanLe on 2023/10/29 18:10.
 * https://github.com/heyanLE
 */
class ComponentBundle(
    private val source: Source
) {

    // 工具类接口
    private val utilsClazz: Set<KClass<*>> = setOf(
        StringHelper::class,
        NetworkHelper::class,
        OkhttpHelper::class,
        PreferenceHelper::class,
        WebViewHelper::class,
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

    private val init = AtomicBoolean(false)


    // 工具注入
    init {
        put(StringHelper::class, StringHelperImpl)
        put(NetworkHelper::class, KoinPlatform.getKoin().get())
        put(OkhttpHelper::class, KoinPlatform.getKoin().get())
        put(PreferenceHelper::class, KoinPlatform.getKoin().get(named(source.key)))
        put(WebViewHelper::class, WebViewHelperImpl)
    }

    private fun put(clazz: KClass<*>, instance: Any) {
        bundle[clazz] = instance
    }

    private fun init(){
        if(init.compareAndSet(false, true)){
            registerClazz.forEach {
                if(innerGet(it) == null){
                    throw SourceException("Component 装配错误")
                }
            }
        }
    }

    private fun innerGet(clazz: KClass<*>, road: MutableSet<KClass<*>> = hashSetOf()): Any? {

        // source 本身可直接注入
        if(clazz.isInstance(source)){
            return source
        }

        // 不允许注入除 工具，Component 以及 source 里 register 以外的类
        if(!utilsClazz.contains(clazz) && !componentClazz.contains(clazz) && registerClazz.contains(clazz) && clazz != ComponentWrapper::class){
            throw SourceException("尝试非法注入： ${clazz.simpleName}")
        }

        // 循环依赖
        if(road.contains(clazz)){
            throw SourceException("${clazz.simpleName} 存在循环依赖")
        }
        if(bundle.contains(clazz)){
            return bundle[clazz]
        }
        val cons = clazz.constructors
        // 只支持一个构造方法
        if(cons.size != 1){
            throw SourceException("${clazz.simpleName} 有多个构造方法")
        }
        val con = cons.first()
        val params = con.parameters
        val targetParams = arrayListOf<Any>()
        road.add(clazz)
        for (param in params) {
            // 只支持普通构造函数传参
            if(param.kind != KParameter.Kind.VALUE){
                throw SourceException("${clazz.simpleName} 构造方法有特殊传参")
            }
            // 构造出错
            val instance = innerGet(clazz, road) ?: throw SourceException("${clazz.simpleName} 装配错误")
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
            if(it.isInstance(instance)){
                throw SourceException("${clazz.simpleName} 实现了 utils 中的接口")
            }
        }
        bundle[clazz] = instance
        componentClazz.forEach {
            if(it.isInstance(instance)){
                bundle[it] = instance
            }
        }

        // ComponentWrapper 自动装配 source
        if(instance is ComponentWrapper) {
            instance.innerSource = source
        }
        return instance
    }

    fun get(clazz: KClass<*>): Any? {
        init()
        if(bundle.contains(clazz)){
            return bundle[clazz]
        }
        return innerGet(clazz, arraySetOf())
    }

    inline fun <reified T> get(): T? {
        return get(T::class) as? T
    }

    fun release(){
        bundle.clear()
    }

}