package org.easybangumi.next.shared.compose.bangumi

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

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
private val bangumiContainerColor = androidx.compose.ui.graphics.Color(0xFFFCEDF0)
private val bangumiColor = androidx.compose.ui.graphics.Color(0xFFED7098)

val ColorScheme.bangumiContainer
    get() = bangumiContainerColor

val ColorScheme.onBangumiContainer
    get() = bangumiColor

val ColorScheme.bangumi
    get() = bangumiColor

val ColorScheme.onBangumi
    get() = Color.White