package org.easybangumi.next.shared.plugin.core.source.loader

import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.plugin.core.component.ComponentBundle
import org.easybangumi.next.shared.plugin.core.info.SourceConfig
import org.easybangumi.next.shared.plugin.core.info.SourceInfo
import org.easybangumi.next.shared.plugin.core.inner.InnerSource
import org.easybangumi.next.shared.plugin.core.inner.InnerSourceWrapper
import org.easybangumi.next.shared.plugin.core.utils.PluginPathProvider


/**
 * Created by heyanlin on 2025/1/29.
 */
class InnerSourceLoader() {

    private val cache = mutableMapOf<String, SourceInfo>()

    fun load(
        innerSource: InnerSource,
        sourceConfig: SourceConfig,
    ): SourceInfo {
        // 缓存
        val ca = cache[innerSource.key]
        if (ca != null && innerSource.manifest.lastModified == ca.manifest.lastModified){
            return ca
        }
        removeCache(innerSource.key)

        // 关闭
        if (!sourceConfig.enable) {
            return SourceInfo.Unable(innerSource.manifest, sourceConfig)
        }


        try {

            val sourceWrapper = InnerSourceWrapper(
                innerSource,
                PluginPathProvider.getSourceWorkPath(innerSource)
            )

            // 加载
            val bundle = ComponentBundle(
                sourceWrapper,
                innerSource.componentConstructor
            )

            bundle.load()
            val info = SourceInfo.Loaded(sourceWrapper.manifest, sourceConfig, bundle)
            cache[innerSource.key] = info
            return info
        } catch (e: Exception){
            return SourceInfo.Error(innerSource.manifest, sourceConfig, e.message ?: "Unknown error", e)
        }

    }

    fun removeCache(key: String){
        cache.remove(key).let {
            if (it is SourceInfo.Loaded){
                it.componentBundle.release()
            }
        }
    }

}