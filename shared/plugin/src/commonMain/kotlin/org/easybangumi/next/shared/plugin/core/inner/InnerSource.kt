package org.easybangumi.next.shared.plugin.core.inner

import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import kotlin.reflect.KClass

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
abstract class InnerSource: Source {

    companion object {


        val InnerSourceList: List<InnerSource> by lazy {
            listOf<InnerSource>(

            )
        }

        const val INNER_EXTENSION_KEY = "inner"
        val InnerExtensionManifest: ExtensionManifest by lazy {
            ExtensionManifest(
                key = INNER_EXTENSION_KEY,
                status = ExtensionManifest.STATUS_CAN_LOAD,
                errorMsg = null,

                label = "内置拓展",
                readme = null,
                author = "Heyanle",
                icon = null,
                versionCode = 1,
                libVersion = 1,
                map = emptyMap(),

                providerType = ExtensionManifest.PROVIDER_TYPE_INNER,
                loadType = ExtensionManifest.LOAD_TYPE_JS_PKG,

                sourcePath = null,
                assetsPath = null,

                workPath = pathProvider.getFilePath("inner_ext"),

                lastModified = Clock.System.now().toEpochMilliseconds(),
            )
        }
    }
    abstract val id: String
    abstract val label: ResourceOr
    abstract val icon: ResourceOr?
    abstract val version: Int

    open val description: String? = null
    open val website: String? = null
    open val author: String = "Heyanle"

    abstract val componentConstructor: Array<()-> Component>

    override val manifest: SourceManifest by lazy {
        SourceManifest(
            id = id,
            label = label,
            icon = icon,
            version = version,
            author = author,
            description = description,
            website = website,
            map = emptyMap(),
            lastModified = Clock.System.now().toEpochMilliseconds(),
            loadType = SourceManifest.LOAD_TYPE_INNER,
            extensionManifest = InnerExtensionManifest
        )
    }

    override val workPath: UFD
        get() = throw IllegalStateException("workPath not support in inner source, please use InnerSourceWrapper")



}