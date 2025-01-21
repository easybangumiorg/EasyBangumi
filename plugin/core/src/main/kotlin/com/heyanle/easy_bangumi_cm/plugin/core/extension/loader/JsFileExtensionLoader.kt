package com.heyanle.easy_bangumi_cm.plugin.core.extension.loader

import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.core.entity.ExtensionInfo
import com.heyanle.easy_bangumi_cm.plugin.core.utils.JsHelper
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
        val sourceManifest = loadFile(extensionManifest, file) ?: return ExtensionInfo.LoadedError(extensionManifest, "file load error")
        return ExtensionInfo.Loaded(extensionManifest, listOf(sourceManifest))
    }

    fun loadFile(extensionManifest: ExtensionManifest, file: File): SourceManifest? {
        val (isCry, manifest) = file.inputStream().use {
            val isCry = JsHelper.isCryptJs(it)
            val manifest = if (isCry) {
                JsHelper.getManifestFromCry(it)
            } else {
                JsHelper.getManifestFromNormal(it)
            }
            isCry to manifest
        }
        return SourceManifest(
            id = manifest["id"] ?: return null,
            type = manifest["type"] ?: Source.TYPE_MEDIA,
            label = manifest["label"] ?: return null,
            version = manifest["version"]?.toIntOrNull() ?: 0,
            extensionManifest = extensionManifest,
            author = manifest["author"] ?: "",
            description = manifest["description"] ?: "",
            icon = manifest["icon"],
            website = manifest["website"],
            map = manifest,
            loadType = if (isCry) SourceManifest.LOAD_TYPE_CRY_JS else SourceManifest.LOAD_TYPE_JS,
            sourceUri = file.absolutePath
        )
    }
}