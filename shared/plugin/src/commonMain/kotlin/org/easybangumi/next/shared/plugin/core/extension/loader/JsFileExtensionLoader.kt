package org.easybangumi.next.shared.plugin.core.extension.loader

import okio.buffer
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.shared.plugin.api.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.info.ExtensionInfo
import org.easybangumi.next.shared.plugin.core.javascript.JsHelper

class JsFileExtensionLoader: ExtensionLoader {

    override fun loadType(): Int {
        return ExtensionManifest.LOAD_TYPE_JS_FILE
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
        val manifest = JsHelper.getManifestFromNormal(source)

        val label = manifest["label"] ?: return null
        return SourceManifest(
            id = manifest["id"] ?: return null,
            label = { label },
            version = manifest["version"]?.toIntOrNull() ?: 0,
            extensionManifest = extensionManifest,
            author = manifest["author"] ?: "",
            description = manifest["description"] ?: "",
            icon = manifest["icon"],
            website = manifest["website"],
            map = manifest,
            loadType = SourceManifest.LOAD_TYPE_JS,
            loaderParam = SourceManifest.LoaderParam.JSLoaderParam(
                ufd = file.getUFD()
            ),
            lastModified = extensionManifest.lastModified
        )
    }
}