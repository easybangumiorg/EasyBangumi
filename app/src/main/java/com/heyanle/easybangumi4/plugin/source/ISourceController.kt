package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
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
interface ISourceController {

    sealed class SourceInfoState {
        data object Loading : SourceInfoState()

        class Info(val info: List<SourceInfo>) : SourceInfoState()
    }

    val sourceInfo: StateFlow<SourceInfoState>

    val configSource: StateFlow<List<ConfigSource>>

    val sourceBundle: StateFlow<SourceBundle?>

}