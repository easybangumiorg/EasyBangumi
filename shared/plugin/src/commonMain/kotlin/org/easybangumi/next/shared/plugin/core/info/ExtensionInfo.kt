package org.easybangumi.next.shared.plugin.core.info

import org.easybangumi.next.shared.plugin.api.extension.ExtensionManifest
import org.easybangumi.next.shared.plugin.api.source.SourceManifest


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

sealed class ExtensionInfo {

    data class Loaded(
        val manifest: ExtensionManifest,
        val sources: List<SourceManifest>,
    ) : ExtensionInfo()

    data class LoadedError(
        val manifest: ExtensionManifest,
        val errMsg: String,
        val exception: Throwable? = null,
    ) : ExtensionInfo()

    fun manifest(): ExtensionManifest {
        return when (this) {
            is Loaded -> manifest
            is LoadedError -> manifest
        }
    }

}