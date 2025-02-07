package com.heyanle.easy_bangumi_cm.common.plugin.core.inner

import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.api.source.SourceManifest
import com.heyanle.lib.inject.core.Inject
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2025/1/29.
 */
abstract class InnerSource: Source {

    private val innerExtensionManifestProvider: InnerExtensionManifestProvider by Inject.injectLazy()

    abstract val id: String
    abstract val label: ResourceOr
    abstract val icon: ResourceOr?
    abstract val version: Int

    open val type = Source.TYPE_MEDIA
    open val description: String? = null
    open val website: String? = null
    open val author: String = "HeYanLe"

    abstract val componentClazz: List<KClass<out Component>>

    override val manifest: SourceManifest = SourceManifest(
        id = id,
        type = type,
        label = label,
        icon = icon,
        version = version,
        author = author,
        description = description,
        website = website,
        map = emptyMap(),
        lastModified = System.currentTimeMillis(),
        loadType = SourceManifest.LOAD_TYPE_INNER,
        sourceUri = "",
        extensionManifest = innerExtensionManifestProvider.extensionManifest
    )

}