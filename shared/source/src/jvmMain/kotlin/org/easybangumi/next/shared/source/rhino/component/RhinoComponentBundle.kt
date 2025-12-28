package org.easybangumi.next.shared.source.rhino.component

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import okio.buffer
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.unifile.fromUFDOrThrow
import org.easybangumi.next.rhino.RhinoRuntime
import org.easybangumi.next.rhino.RhinoScope
import org.easybangumi.next.shared.source.ConstClazz
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.component.ComponentBundle
import org.easybangumi.next.shared.source.api.component.ComponentBundleException
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.source.SourceManifest
import org.easybangumi.next.shared.source.core.source.ManifestSource
import org.easybangumi.next.shared.source.core.source.SourceLibWrapper
import org.easybangumi.next.shared.source.core.utils.utilsModule
import org.easybangumi.next.shared.source.rhino.RhinoConst
import org.koin.core.Koin
import org.koin.dsl.binds
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass
import kotlin.text.set

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
    private val sourceManifest: SourceManifest,
    private val rhinoRuntime: RhinoRuntime,
): ComponentBundle {

    private val logger = logger()

    @Volatile
    private var koinApp: Koin? = null

    private val componentBusiness: HashMap<KClass<out Component>, ComponentBusiness<out Component>> = hashMapOf()

    private val loaded = atomic(false)

    @Volatile
    private var rhinoScope: RhinoScope? = null

    private val sourceWrapper: Source by lazy {
        SourceLibWrapper(ManifestSource(sourceManifest))
    }

    override fun getSource(): Source {
        return sourceWrapper
    }


    suspend fun loadAsync() {
        if (loaded.compareAndSet(expect = false, update = true)) {
            runBlocking {
                var rawString: String? = sourceManifest.param as? String
                var pluginFile: UniFile? = null
                val ufd = sourceManifest.param as? UFD
                if (ufd == null && rawString == null) {
                    throw ComponentBundleException("RhinoComponentBundle load error, ufd and rawString are both null")
                }
                if (rawString == null && ufd != null) {
                    val file = UniFileFactory.fromUFD(ufd) ?: throw ComponentBundleException("RhinoComponentBundle load error, ufd is invalid $ufd")
                    if ( !file.exists() || file.isDirectory() || !file.canRead()) {
                        throw ComponentBundleException("RhinoComponentBundle load error, ufd is not a valid file: $ufd")
                    }
                    pluginFile = file
                }

                val rs = RhinoScope(rhinoRuntime)
                if (!rs.init()) {
                    throw ComponentBundleException("RhinoScope init error")
                }
                rhinoScope = rs

                val ko = koinApplication {
                    val component = module {
                        // Load source
                        single {
                            sourceWrapper
                        }.binds(ConstClazz.sourceClazz.toTypedArray())


                    }
                    // load utils
                    modules(component, utilsModule)

                }.koin


                koinApp = ko

                rs.runWithScope { ctx, scriptable ->
                    // 1. rhino 加载 Import
                    ctx.evaluateString(
                        scriptable,
                        RhinoConst.RHINO_IMPORT_STRING,
                        "Source(${sourceManifest.key})",
                        1, 0
                    )

                    // 2. rhino 加载插件文件
                    if (rawString != null) {
                        ctx.evaluateString(
                            scriptable,
                            rawString,
                            "Source(${sourceManifest.key})",
                            1, 0
                        )
                    } else if (pluginFile != null) {
                        pluginFile.openSource().buffer().inputStream().reader().use {
                            ctx.evaluateReader(
                                scriptable,
                                it,
                                "Source(${sourceManifest.key})",
                                1, 0
                            )
                        }

                    }


                    // 3. 创建 Component Wrapper
                    val componentList = RhinoConst.rhinoComponentFactoryClazz.mapNotNull {
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
    }

    override suspend fun load() {
        loadAsync()
    }

    override fun <T : Any> get(clazz: KClass<T>): T? {
        if (!loaded.value) return null
        // 获取 Component 请使用 getBusiness
        if (ConstClazz.componentClazz.contains(clazz)) {
            return null
        }
        return koinApp?.get(clazz)
    }

    override fun <T : Component> getBusiness(clazz: KClass<T>): ComponentBusiness<T>? {
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
        if (loaded.compareAndSet(true, false)) {
            koinApp?.close()
            koinApp = null
            componentBusiness.clear()
            rhinoScope?.release()
        }
    }
}