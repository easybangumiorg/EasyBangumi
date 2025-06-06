package org.easybangumi.next.shared.plugin.core.javascript.rhino.source

import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.info.SourceConfig
import org.easybangumi.next.shared.plugin.core.info.SourceInfo
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

class RhinoCryLoader(
    private val jsLoader: RhinoSourceLoader,
): SourceLoader {

    override fun loadType(): Int = SourceManifest.LOAD_TYPE_CRY_JS

    override suspend fun load(
        sourceManifest: SourceManifest,
        sourceConfig: SourceConfig
    ): SourceInfo {
        TODO("Not yet implemented")
    }

    override fun removeCache(key: String) {
        TODO("Not yet implemented")
    }
}