package org.easybangumi.next.shared.source.rhino

import org.easybangumi.next.rhino.RhinoRuntimeProvider
import org.easybangumi.next.shared.source.api.component.ComponentBundle
import org.easybangumi.next.shared.source.api.source.SourceManifest
import org.easybangumi.next.shared.source.plugin.PluginLoader
import org.easybangumi.next.shared.source.rhino.component.RhinoComponentBundle

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

class RhinoPluginLoaderFactory: PluginLoader.Factory {

    private val runtimeProvider = RhinoRuntimeProvider()

    override fun create(sourceManifest: SourceManifest): PluginLoader {
        return object: PluginLoader {
            override suspend fun load(): ComponentBundle {
                val bundle = RhinoComponentBundle(
                    sourceManifest,
                    runtimeProvider.getRuntime()
                )
                bundle.loadAsync()
                return bundle
            }
        }
    }

}