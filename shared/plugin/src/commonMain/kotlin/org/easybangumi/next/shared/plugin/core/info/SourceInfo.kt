package org.easybangumi.next.shared.plugin.core.info

import org.easybangumi.next.shared.plugin.api.component.ComponentBundle
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.component.SimpleComponentBundle


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

sealed class SourceInfo {

    abstract val manifest: SourceManifest
    abstract val sourceConfig: SourceConfig

    // 加载成功
    class Loaded(
        override val manifest: SourceManifest,
        override val sourceConfig: SourceConfig,
        val componentBundle: ComponentBundle,
    ) : SourceInfo()

    // 被阻断（用户主动关闭或者崩溃后关闭）
    class Unable(
        override val manifest: SourceManifest,
        override val sourceConfig: SourceConfig,
    ) : SourceInfo()

    // 加载失败
    class Error(
        override val manifest: SourceManifest,
        override val sourceConfig: SourceConfig,
        val msg: String,
        val exception: Exception? = null,
    ) : SourceInfo()


}