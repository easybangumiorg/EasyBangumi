package org.easybangumi.next.shared.plugin.core.javascript.rhino.source

import org.easybangumi.next.rhino.RhinoRuntimeProvider
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.info.SourceConfig
import org.easybangumi.next.shared.plugin.core.info.SourceInfo
import org.easybangumi.next.shared.plugin.core.javascript.rhino.component.RhinoComponentBundle
import org.easybangumi.next.shared.plugin.core.source.loader.SourceLoader

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

class RhinoSourceLoader(
    private val rhinoRuntimeProvider: RhinoRuntimeProvider,
) : SourceLoader {

    private val cache = mutableMapOf<String, SourceInfo>()

    override fun loadType(): Int = SourceManifest.LOAD_TYPE_JS

    override suspend fun load(
        sourceManifest: SourceManifest,
        sourceConfig: SourceConfig
    ): SourceInfo {
        TODO()
//        // 缓存
//        val ca = cache[sourceManifest.key]
//        if (ca != null && sourceManifest.lastModified == ca.manifest.lastModified){
//            return ca
//        }
//        removeCache(sourceManifest.key)
//
//        // 关闭
//        if (!sourceConfig.enable) {
//            return SourceInfo.Unable(sourceManifest, sourceConfig)
//        }
//
//        val param = sourceManifest.loaderParam as? SourceManifest.LoaderParam.JSLoaderParam
//        if (param == null) {
//            return SourceInfo.Error(
//                sourceManifest,
//                sourceConfig,
//                "Invalid loader parameter for JavaScript source",
//                IllegalArgumentException("Invalid loader parameter for JavaScript source")
//            )
//        }
//
//        try {
//            val runtime = rhinoRuntimeProvider.getRuntime()
//            val bundle = RhinoComponentBundle(
//                sourceManifest,
//                param,
//                runtime
//            )
//
//            // 加载
//            val info = SourceInfo.Loaded(sourceWrapper.manifest, sourceConfig, sourceWrapper)
//            cache[sourceManifest.key] = info
//            return info
//        } catch (e: Exception) {
//            return SourceInfo.Error(sourceManifest, sourceConfig, e.message ?: "Unknown error", e)
//        }

    }

    override fun removeCache(key: String) {
        cache.remove(key)
    }
}