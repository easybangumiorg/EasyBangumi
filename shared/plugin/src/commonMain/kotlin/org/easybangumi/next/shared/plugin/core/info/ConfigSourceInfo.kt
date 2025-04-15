package org.easybangumi.next.shared.plugin.core.info

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

class ConfigSourceInfo(
    val config: SourceConfig,
    val sourceInfo: SourceInfo
) {
    val sourceManifest: SourceManifest
        get() = sourceInfo.manifest
}


data class SourceConfig(
    val key: String,
    val order: Long,
    val enable: Boolean,
){

}