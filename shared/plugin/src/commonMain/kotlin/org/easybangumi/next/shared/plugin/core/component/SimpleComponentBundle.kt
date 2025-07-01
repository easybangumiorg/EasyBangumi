package org.easybangumi.next.shared.plugin.core.component

import kotlinx.atomicfu.atomic
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.plugin.api.ConstClazz
import org.easybangumi.next.shared.plugin.api.component.BaseComponent
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.component.ComponentBundle
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.utils.utilsModule
import org.koin.core.Koin
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.concurrent.Volatile
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
class SimpleComponentBundle(
    private val source: Source,
    private val componentConstructor: Array<() -> Component>,
): ComponentBundle {
    private val logger = logger()

    @Volatile
    private var koinApp: Koin? = null

    private val componentBusiness: HashMap<KClass<out Component>, ComponentBusiness<out Component>> = hashMapOf()

    private val init = atomic(false)

    override suspend fun load(){

        if (init.compareAndSet(expect = false, update = true)) {

            val realComponent = componentConstructor.map { it() }

            koinApp = koinApplication {
                val component = module {
                    // Load source
                    single {
                        source
                    }.binds(ConstClazz.sourceClazz.toTypedArray())

                    // Load component
                    realComponent.forEach { component ->
                        single {
                            component.apply {
                                if (this is BaseComponent) {
                                    innerSource = this@SimpleComponentBundle.source
                                    innerKoin = koin
                                }
                            }
                        }.binds(
                            ConstClazz.componentClazz.filter {
                                it.isInstance(component)
                            }.toTypedArray().apply {
                                logger.info("load component: ${component::class.simpleName} -> ${this.joinToString(",")}")
                            }
                        )
                    }


                }

                modules(component, utilsModule)

                // Load source module if exists
                source.module ?.let {
                    modules(it)
                }

            }.koin
        }
    }


    override fun getSource(): Source {
        return source
    }



    override fun <T : Any> get(clazz: KClass<T>): T? {
        if (!init.value) return null
        // 获取 Component 请使用 getBusiness
        if (ConstClazz.componentClazz.contains(clazz)) {
            return null
        }
        return koinApp?.get(clazz)
    }

    override fun <T: Component> getBusiness(clazz: KClass<T>): ComponentBusiness<T>? {
        if (!init.value) return null
        val cache = componentBusiness[clazz]
        if (cache != null) {
            return cache as? ComponentBusiness<T>
        }
        val component = koinApp?.getOrNull<T>(clazz) ?: return null
        val business = ComponentBusiness(component)
        componentBusiness[clazz] = business
        return business
    }


    override fun release() {
        if (init.compareAndSet(true, false)) {
            koinApp?.close()
            koinApp = null
            componentBusiness.clear()
        }
    }
}