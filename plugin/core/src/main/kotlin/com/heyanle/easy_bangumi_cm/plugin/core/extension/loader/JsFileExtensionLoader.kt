package com.heyanle.easy_bangumi_cm.plugin.core.extension.loader

import com.heyanle.easy_bangumi_cm.plugin.core.entity.ExtensionInfo
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest
import java.io.File

/**
 * Created by heyanlin on 2024/12/18.
 */
class JsFileExtensionLoader: ExtensionLoader {

    override fun loadType(): Int {
        return ExtensionManifest.LOAD_TYPE_JS_FILE
    }

    override suspend fun load(extensionManifest: ExtensionManifest): ExtensionInfo {
        val file = File(extensionManifest.workPath)
        if(!file.exists() || !file.isFile){
            return ExtensionInfo.LoadedError(extensionManifest, "file not exists")
        }
        val sourceManifest = loadFile(file) ?: return ExtensionInfo.LoadedError(extensionManifest, "file load error")
        return ExtensionInfo.Loaded(extensionManifest, listOf(sourceManifest))
    }

    fun loadFile(file: File): SourceManifest? {
        return null
    }
}