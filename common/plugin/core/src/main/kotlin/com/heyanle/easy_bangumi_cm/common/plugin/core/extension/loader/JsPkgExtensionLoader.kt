package com.heyanle.easy_bangumi_cm.common.plugin.core.extension.loader

import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.ExtensionInfo
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import java.io.File

/**
 * Created by heyanlin on 2024/12/18.
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
        val jsFileFolder = File(extensionManifest.workPath, SCRIPT_NAME)
        if(!jsFileFolder.exists() || !jsFileFolder.isDirectory){
            return ExtensionInfo.LoadedError(extensionManifest, "script folder not exists")
        }
        val sourceManifestList = jsFileFolder.listFiles()?.filter { it.isFile }?.map { jsFileExtensionLoader.loadFile(extensionManifest, it) }?.filterNotNull() ?: emptyList()
        if (sourceManifestList.isEmpty()){
            return ExtensionInfo.LoadedError(extensionManifest, "script file load error")
        }
        return ExtensionInfo.Loaded(extensionManifest, sourceManifestList)
    }
}