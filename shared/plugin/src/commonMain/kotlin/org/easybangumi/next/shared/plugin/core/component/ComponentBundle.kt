package org.easybangumi.next.shared.plugin.core.component

import kotlinx.atomicfu.atomic
import org.easybangumi.next.shared.plugin.api.ConstClazz
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.core.safe.makeComponentProxy
import org.easybangumi.next.shared.plugin.utils.UtilsProvider
import org.koin.core.Koin
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module
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
class ComponentBundle(
    private val source: Source,
    private val componentConstructor: Array<() -> Component>,
) {
    private var koinApp: Koin? = null

    // proxy 不使用 koin 管理，保证业务内无法获取 Proxy，只能获取原始对象
    private val componentProxy: HashMap<KClass<out Component>, Component> = hashMapOf()

    private val init = atomic(false)

    fun load(){

        if (init.compareAndSet(false, true)) {

            val realComponent = componentConstructor.map { it() }

            koinApp = koinApplication {
                module {
                    // Load source
                    single {
                        source
                    }.binds(ConstClazz.sourceClazz)

                    // Load component
                    realComponent.forEach { component ->
                        single {
                            component.apply {
                                if (this is ComponentWrapper) {
                                    innerSource = source
                                    innerKoin = koin
                                }
                            }
                        }.binds(
                            ConstClazz.componentClazz.filter {
                                it.isInstance(component)
                            }.toTypedArray()
                        )
                    }

                    // Load utils
                    ConstClazz.utilsClazz.forEach { clazz ->
                        val util = UtilsProvider.get(clazz, source)
                        if (util != null) {
                            single {
                                util
                            }.binds(arrayOf(clazz))
                        }
                    }


                }
            }.koin
        }
    }


    fun getSource(): Source {
        return source
    }

    fun <T : Any> get(clazz: KClass<T>): T? {
        if (!init.value) return null
        return koinApp?.get(clazz)
    }

    fun <T: Component> getIfProxy(clazz: KClass<T>): T? {
        if (!init.value) return null
        val cache = componentProxy[clazz]
        if (cache != null) {
            return cache as? T
        }
        val component = koinApp?.get<T>(clazz) ?: return null
        // proxy 失败直接放弃返回原对象，只尝试一次
        val proxy = makeComponentProxy(component) ?: component
        componentProxy[clazz] = proxy
        return proxy as? T
    }

    fun release() {
        if (init.compareAndSet(true, false)) {
            koinApp?.close()
            koinApp = null
            componentProxy.clear()
        }
    }
}