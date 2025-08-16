package com.heyanle.easybangumi4.plugin.extension

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
interface IExtensionController {

    data class ExtensionState(
        val loading: Boolean = true,
        val extensionInfoMap: Map<String, ExtensionInfo> = emptyMap()
    )

    val state: StateFlow<ExtensionState>

    fun init()
}