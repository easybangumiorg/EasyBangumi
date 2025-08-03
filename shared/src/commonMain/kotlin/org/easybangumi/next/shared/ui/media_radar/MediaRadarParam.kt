package org.easybangumi.next.shared.ui.media_radar

import kotlinx.serialization.Serializable
import org.easybangumi.next.shared.data.cartoon.CartoonCover

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
@Serializable
data class MediaRadarParam (
    val cover: CartoonCover,
    val userKeyword: String = "", // use cover.name if empty
    val subKeyword: List<String> = emptyList(),
    val limitWhichKeyword: Int = 0, // 0 means no limit
)