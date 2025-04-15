package org.easybangumi.next.shared.plugin.core.extension.loader

import okio.buffer
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.shared.plugin.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.info.ExtensionInfo
import org.easybangumi.next.shared.plugin.javascript.JsHelper
import org.easybangumi.next.shared.plugin.source.SourceManifest

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

class JsFileCryExtensionLoader: ExtensionLoader {

    override fun loadType(): Int {
        return ExtensionManifest.LOAD_TYPE_JS_CRY_FILE
    }

    override suspend fun load(extensionManifest: ExtensionManifest): ExtensionInfo {
        val file = UniFileFactory.fromUFD(extensionManifest.workPath)
        if(file == null || !file.exists() || !file.isFile()){
            return ExtensionInfo.LoadedError(extensionManifest, "file not exists")
        }
        val sourceManifest = loadFile(extensionManifest, file) ?: return ExtensionInfo.LoadedError(extensionManifest, "file load error")
        return ExtensionInfo.Loaded(extensionManifest, listOf(sourceManifest))
    }

    fun loadFile(extensionManifest: ExtensionManifest, file: UniFile): SourceManifest? {
        val source = file.openSource().buffer()
        val manifest = JsHelper.getManifestFromCry(source)

        val label = manifest["label"] ?: return null
        return SourceManifest(
            id = manifest["id"] ?: return null,
            type = manifest["type"] ?: return null,
            label = { label },
            version = manifest["version"]?.toIntOrNull() ?: 0,
            extensionManifest = extensionManifest,
            author = manifest["author"] ?: "",
            description = manifest["description"] ?: "",
            icon = manifest["icon"],
            website = manifest["website"],
            map = manifest,
            loadType = SourceManifest.LOAD_TYPE_CRY_JS,
            sourceUri = file.getUFD(),
            lastModified = extensionManifest.lastModified
        )
    }
}