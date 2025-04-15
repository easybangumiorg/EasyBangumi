package org.easybangumi.next.shared.plugin.core.component

import kotlinx.atomicfu.atomic
import org.easybangumi.next.shared.plugin.api.ConstClazz
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.core.EasyPluginConfigProvider
import org.koin.dsl.koinApplication
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
    private val utilsProvider: EasyPluginConfigProvider.UtilsProvider,
    private val componentConstructor: Array<() -> Component>,
) {

    private val bundleMap: HashMap<KClass<*>, Any> = hashMapOf()
    private val componentClazz: HashMap<KClass<*>, KClass<*>> = hashMapOf()

    private val init = atomic(false)

    fun load(){
        if (init.compareAndSet(false, true)) {
            // Load source
            bundleMap[source::class] = source
            ConstClazz.sourceClazz.forEach {
                if (it.isInstance(source)) {
                    bundleMap[it] = source
                }
            }

            // Load utils
            ConstClazz.utilsClazz.forEach {
                val util = utilsProvider.get(it, source)
                if (util != null) {
                    bundleMap[it] = util
                }
            }

            // Load components
            val componentList = componentConstructor.map { it() }
            componentList.forEach { nc ->
                val clazz = nc::class
                if (bundleMap[clazz] == null) {
                    bundleMap[clazz] = nc
                }
                if (componentClazz[clazz] == null) {
                    componentClazz[clazz] = clazz
                }
                ConstClazz.componentClazz.forEach {
                    if (it.isInstance(nc)) {
                        bundleMap[it] = nc
                        componentClazz[it] = clazz
                    }
                }
                if (nc is ComponentWrapper) {
                    nc.innerSource = source
                    nc.innerBundle = this
                }
            }
        }
    }


    fun getSource(): Source {
        return source
    }

    fun <T : Any> get(clazz: KClass<T>): T? {
        return bundleMap[clazz] as? T
    }

    fun release() {
        init.getAndSet(false)
        bundleMap.clear()
        componentClazz.clear()
    }
}