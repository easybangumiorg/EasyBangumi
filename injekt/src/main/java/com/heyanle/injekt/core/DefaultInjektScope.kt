package com.heyanle.injekt.core

import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.InjektionException
import com.heyanle.injekt.api.TypeReference
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by HeYanLe on 2023/7/29 19:38.
 * https://github.com/heyanLE
 */
class DefaultInjektScope : InjektScope() {

    object NoKey {}

    internal data class Instance(val forWhatType: Type, val forKey: Any)

    // Get with type checked key
    private fun <K : Any, V : Any> Map<K, V>.getByKey(key: K): V? = get(key)

    // 存储 实体类
    private val existingValues = ConcurrentHashMap<Instance, Any>()
    private val threadedValues = object : ThreadLocal<HashMap<Instance, Any>>() {
        override fun initialValue(): HashMap<Instance, Any> {
            return hashMapOf()
        }
    }


    // 类型 -> 工厂
    private val factories = ConcurrentHashMap<Type, () -> Any>()
    private val keyedFactories = ConcurrentHashMap<Type, (Any) -> Any>()

    // factory
    override fun <R : Any> getInstance(forType: Type): R {
        val factory = factories.getByKey(forType)
            ?: throw InjektionException("No registered instance or factory for type ${forType}")
        return factory.invoke() as R
    }

    override fun <R : Any, K : Any> getKeyedInstance(forType: Type, key: K): R {
        val factory = keyedFactories.getByKey(forType)
            ?: throw InjektionException("No registered keyed factory for type ${forType}")
        return factory.invoke(key) as R
    }

    // registry

    override fun <R : Any> addSingletonFactory(
        forType: TypeReference<R>,
        factoryCalledOnce: () -> R
    ) {
        factories[forType.type] = {
            (existingValues.getOrPut(
                Instance(
                    forType.type,
                    NoKey
                )
            ) { lazy { factoryCalledOnce() } } as Lazy<R>).value
        }
    }

    override fun <R : Any> addFactory(forType: TypeReference<R>, factoryCalledEveryTime: () -> R) {
        factories[forType.type] = factoryCalledEveryTime
    }

    override fun <R : Any> addPerThreadFactory(
        forType: TypeReference<R>,
        factoryCalledOncePerThread: () -> R
    ) {
        factories[forType.type] = {
            threadedValues.get()
                .getOrPut(Instance(forType.type, NoKey)) { factoryCalledOncePerThread() }
        }
    }

    override fun <R : Any, K : Any> addPerKeyFactory(
        forType: TypeReference<R>,
        factoryCalledPerKey: (K) -> R
    ) {
        keyedFactories[forType.type] = { key ->
            existingValues.getOrPut(Instance(forType.type, key)) { factoryCalledPerKey(key as K) }
        }
    }

    override fun <R : Any, K : Any> addPerThreadPerKeyFactory(
        forType: TypeReference<R>,
        factoryCalledPerKeyPerThread: (K) -> R
    ) {
        keyedFactories[forType.type] = { key ->
            threadedValues.get()
                .getOrPut(Instance(forType.type, key)) { factoryCalledPerKeyPerThread(key as K) }
        }
    }

    override fun <T : Any> hasFactory(forType: TypeReference<T>): Boolean {
        return factories.getByKey(forType.type) != null || keyedFactories.getByKey(forType.type) != null
    }

    override fun <O: Any, T: O> addAlias(existingRegisteredType: TypeReference<T>, otherAncestorOrInterface: TypeReference<O>) {
        // factories existing or not, and data type compatibility is checked in the InjektRegistrar interface default methods
        val existingFactory = factories.getByKey(existingRegisteredType.type)
        val existingKeyedFactory = keyedFactories.getByKey(existingRegisteredType.type)

        if (existingFactory != null) {
            factories[otherAncestorOrInterface.type] = existingFactory
        }
        if (existingKeyedFactory != null) {
            keyedFactories[otherAncestorOrInterface.type] = existingKeyedFactory
        }
    }
}