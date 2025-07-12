package org.easybangumi.next.shared.source.core.inner

import kotlinx.atomicfu.atomic
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.source.ConstClazz
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.component.ComponentBundle
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.source.InnerSource
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.core.SourceLibWrapper
import org.easybangumi.next.shared.source.core.utils.utilsModule
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

class InnerComponentBundle(
    private val innerSource: InnerSource,
): ComponentBundle {

    private val logger = logger()

    private val sourceWrapper: Source by lazy {
        SourceLibWrapper(innerSource)
    }

    @Volatile
    private var koinApp: Koin? = null

    private val componentBusiness: HashMap<KClass<out Component>, ComponentBusiness<out Component>> = hashMapOf()

    private val loaded = atomic(false)


    override fun getSource(): Source {
        return sourceWrapper
    }

    override fun load() {
        if (loaded.compareAndSet(expect = false, update = true)) {
            val realComponent = innerSource.componentConstructor.map { it.invoke() }
            koinApp = koinApplication {
                val component = module {
                    // Load source
                    single {
                        sourceWrapper
                    }.binds(ConstClazz.sourceClazz.toTypedArray())

                    // Load component
                    realComponent.forEach { component ->
                        single {
                            component.apply {
                                if (this is BaseComponent) {
                                    innerSource = sourceWrapper
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
                sourceWrapper.module ?.let {
                    modules(it)
                }

            }.koin

        }
    }

    override fun <T : Any> get(clazz: KClass<T>): T? {
        if (!loaded.value) return null
        // 获取 Component 请使用 getBusiness
        if (ConstClazz.componentClazz.contains(clazz)) {
            return null
        }
        return koinApp?.get(clazz)
    }

    override fun <T: Component> getBusiness(clazz: KClass<T>): ComponentBusiness<T>? {
        if (!loaded.value) return null
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
        if (loaded.compareAndSet(expect = true, update = false)) {
            koinApp?.close()
            koinApp = null
            componentBusiness.clear()
        }
    }
}