package org.easybangumi.next.shared.compose.web

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

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
data class WebPageParam(
    val url: String,
    val title: String? = null,
)

@Composable
expect fun WebPage(param: WebPageParam)