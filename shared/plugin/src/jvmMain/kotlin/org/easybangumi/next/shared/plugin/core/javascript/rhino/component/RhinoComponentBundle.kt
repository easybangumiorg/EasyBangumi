package org.easybangumi.next.shared.plugin.core.javascript.rhino.component

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.TimeoutCancellationException
import okio.buffer
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFDOrThrow
import org.easybangumi.next.rhino.RhinoRuntime
import org.easybangumi.next.rhino.RhinoScope
import org.easybangumi.next.rhino.RhinoScopeException
import org.easybangumi.next.shared.plugin.api.ConstClazz
import org.easybangumi.next.shared.plugin.api.SourceException
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.component.ComponentBundle
import org.easybangumi.next.shared.plugin.core.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.core.javascript.rhino.RhinoConstClazz
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
class RhinoComponentBundle(
    private val source: Source,
    private val param: SourceManifest.LoaderParam.JSLoaderParam,
    private val rhinoRuntime: RhinoRuntime,
) : ComponentBundle {

    private val logger = logger()

    @Volatile
    private var koinApp: Koin? = null

    private val componentBusiness: HashMap<KClass<out Component>, ComponentBusiness<out Component>> = hashMapOf()

    private val init = atomic(false)

    private var rhinoScope: RhinoScope? = null

    override fun getSource(): Source {
        return source
    }

    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    override suspend fun load() {
        if (init.compareAndSet(expect = false, update = true)) {
            if (param.ufd == null && param.rawString == null) {
                return
            }
            val ko = koinApplication {
                val component = module {
                    // Load source
                    single {
                        source
                    }.binds(ConstClazz.sourceClazz.toTypedArray())


                }
                // load utils
                modules(component, utilsModule)

            }.koin
            koinApp = ko

            val rs = RhinoScope(rhinoRuntime)
            if (!rs.init()) {
                throw SourceException("RhinoScope init error")
            }
            rhinoScope = rs




            rs.runWithScope { ctx, scriptable ->

                // 1. rhino 加载 Import
                ctx.evaluateString(
                    scriptable,
                    RHINO_IMPORT_STRING,
                    "Rhino import Source(${source.manifest.id})",
                    1, 0
                )

                // 2. rhino 加载插件文件
                if (param.ufd != null) {
                    val file = UniFileFactory.fromUFDOrThrow(param.ufd)
                    ctx.evaluateReader(
                        scriptable,
                        file.openSource().buffer().inputStream().reader(),
                        "Rhino ${param.ufd} Source(${source.manifest.id})",
                        1, 0
                    )
                }

                if (param.rawString != null) {
                    ctx.evaluateString(
                        scriptable,
                        param.rawString,
                        "Rhino rawString Source(${source.manifest.id})",
                        1, 0
                    )
                }

                // 3. 创建 Component Wrapper
                val componentList = RhinoConstClazz.rhinoComponentFactoryClazz.mapNotNull {
                    it.create(rs)
                }
                val module = module {
                    componentList.forEach { wrapper ->
                        single {
                            wrapper
                        }.binds(wrapper.getComponentClazz())
                    }
                }
                ko.loadModules(listOf(module))
            }
        }
    }

    override fun <T : Any> get(clazz: KClass<T>): T? {
        if (!init.value) return null
        // 获取 Component 请使用 getBusiness
        if (ConstClazz.componentClazz.contains(clazz)) {
            return null
        }
        return koinApp?.get(clazz)
    }

    override fun <T : Component> getBusiness(clazz: KClass<T>): ComponentBusiness<T>? {
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
            rhinoScope?.release()
        }
    }
}