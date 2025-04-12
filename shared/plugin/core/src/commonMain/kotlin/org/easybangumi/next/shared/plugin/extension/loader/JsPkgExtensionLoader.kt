package org.easybangumi.next.shared.plugin.extension.loader

import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.shared.plugin.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.info.ExtensionInfo

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

class JsPkgExtensionLoader(
    private val jsFileExtensionLoader: JsFileExtensionLoader,
): ExtensionLoader {

    companion object {
        const val SCRIPT_NAME = "script"
    }

    override fun loadType(): Int {
        return ExtensionManifest.LOAD_TYPE_JS_PKG
    }

    override suspend fun load(extensionManifest: ExtensionManifest): ExtensionInfo {
        val file = UniFileFactory.fromUFD(extensionManifest.workPath)
        val jsFileFolder = file?.child(SCRIPT_NAME)
        if(jsFileFolder == null || !jsFileFolder.exists() || !jsFileFolder.isDirectory()){
            return ExtensionInfo.LoadedError(extensionManifest, "script folder not exists")
        }
        val sourceManifestList = jsFileFolder.listFiles().filter { it?.isFile() == true }.filterNotNull()
            .mapNotNull { jsFileExtensionLoader.loadFile(extensionManifest, it) }
        if (sourceManifestList.isEmpty()){
            return ExtensionInfo.LoadedError(extensionManifest, "script file load error")
        }
        return ExtensionInfo.Loaded(extensionManifest, sourceManifestList)
    }
}