package org.easybangumi.next.shared.compose.browser

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
data class BrowserPageParam(
    val url: String,
    val title: String? = null,
    val showToolbar: Boolean = true,
    val enableJavaScript: Boolean = true,
    val userAgent: String? = null,
)