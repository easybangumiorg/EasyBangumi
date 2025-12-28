package org.easybangumi.next.shared.source.quick

import com.dokar.quickjs.QuickJs
import kotlinx.atomicfu.atomic
import okio.buffer
import okio.use
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.quickjs.QuickJsFactory
import org.easybangumi.next.shared.source.ConstClazz
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.component.ComponentBundle
import org.easybangumi.next.shared.source.api.component.ComponentBundleException
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.source.SourceManifest
import org.easybangumi.next.shared.source.api.utils.HttpHelper
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.StringHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.core.source.ManifestSource
import org.easybangumi.next.shared.source.core.source.SourceLibWrapper
import org.easybangumi.next.shared.source.core.utils.utilsModule
import org.easybangumi.next.shared.source.plugin.PluginConst
import org.easybangumi.next.shared.source.quick.utils.QuickWebViewHelper
import org.easybangumi.next.shared.source.quick.utils.register
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
class QuickComponentBundle(
    private val sourceManifest: SourceManifest,
    private val quickJsFactory: QuickJsFactory,
): ComponentBundle {

    companion object {
        const val MAX_LOADER_VERSION = 1
        const val MIN_LOADER_VERSION = 1
    }

    private val logger = logger()
    private val loaded = atomic(false)

    @Volatile
    private var koinApp: Koin? = null
    @Volatile
    private var quickJs: QuickJs? = null

    private val componentBusiness: HashMap<KClass<out Component>, ComponentBusiness<out Component>> = hashMapOf()

    private val sourceWrapper: Source by lazy {
        SourceLibWrapper(ManifestSource(sourceManifest))
    }


    override fun getSource(): Source {
        return sourceWrapper
    }

    @Throws(ComponentBundleException::class)
    override suspend fun load() {
        if (loaded.compareAndSet(expect = false, update = true)) {
            sourceManifest.map[PluginConst.MANIFEST_LOADER_VERSION_KEY]?.let {
                val version = it.toInt()
                if (version < MIN_LOADER_VERSION) {
                    throw ComponentBundleException("need upgrade source loader version, current version: $version, min supported version: $MIN_LOADER_VERSION")
                }
                if (version > MAX_LOADER_VERSION) {
                    throw ComponentBundleException("app version is too low to support this source loader version: $version, max supported version: $MAX_LOADER_VERSION")
                }
            }

            var rawString: String? = sourceManifest.param as? String
            var pluginFile: UniFile? = null
            val ufd = sourceManifest.param as? UFD
            if (ufd == null && rawString == null) {
                throw ComponentBundleException("QuickComponentBundle load error, ufd and rawString are both null")
            }
            if (rawString == null && ufd != null) {
                val file = UniFileFactory.fromUFD(ufd) ?: throw ComponentBundleException("RhinoComponentBundle load error, ufd is invalid $ufd")
                if ( !file.exists() || file.isDirectory() || !file.canRead()) {
                    throw ComponentBundleException("QuickComponentBundle load error, ufd is not a valid file: $ufd")
                }
                pluginFile = file
            }

            val qjs = quickJsFactory.createQuickJs()
            if (qjs?.isClosed != false) {
                throw ComponentBundleException("QuickComponentBundle load error, QuickJs create failed")
            }
            quickJs = qjs

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

            // 1.register utils
            val webViewHelper = ko.getOrNull<WebViewHelper>()
            if (webViewHelper != null) {
                qjs.register(QuickWebViewHelper(webViewHelper))
            }

            val preferenceHelper = ko.getOrNull<PreferenceHelper>()
            if (preferenceHelper != null) {
                qjs.register(preferenceHelper)
            }

            val stringHelper = ko.getOrNull<StringHelper>()
            if (stringHelper != null) {
                qjs.register(stringHelper)
            }

            val networkHelper = ko.getOrNull<NetworkHelper>()
            if (networkHelper != null) {
                qjs.register(networkHelper)
            }

            val httpHelper = ko.getOrNull<HttpHelper>()


            val logger = logger("QuickJs-${sourceManifest.key}")
            qjs.register(logger)



            // load

            // 2. load plugin script
            if (rawString != null) {
                qjs.evaluate<Unit>(
                    rawString,
                    "Source-${sourceManifest.key}.js",
                )
            } else pluginFile?.openSource()?.buffer()?.use {
                val code = it.readUtf8()
                qjs.evaluate<Unit>(
                    code,
                    "Source-${sourceManifest.key}.js",
                )
            }

            // 3. create component
            val componentList = QuickConst.quickComponentFactoryClazz.mapNotNull {
                it.create(qjs).apply {
                    // inject koin and source if it is BaseComponent
                    if (this is BaseComponent) {
                        this.innerKoin = ko
                        this.innerSource = sourceWrapper
                    }
                }
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
            quickJs?.close()
        }
    }
}