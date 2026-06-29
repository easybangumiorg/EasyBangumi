package org.easybangumi.next.shared.source.api.source

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

sealed class RemoteSource {
    abstract val remoteInfo: RemoteInfo?
    data class Remote(
        override val remoteInfo: RemoteInfo
    ): RemoteSource()

    data class Local(
        override val remoteInfo: RemoteInfo?,
        val sourceInfo: SourceInfo,
    ): RemoteSource()
}

data class RemoteInfo(
    val key: String,
    val label: String,
    val version: Int,
    val author: String?,
    val repository: String,
    val url: String,
) {
    companion object {
        const val INNER_REPOSITORY = "inner"
        const val ASSETS_REPOSITORY = "assets"
    }
}