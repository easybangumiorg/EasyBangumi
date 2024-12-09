package com.heyanle.easy_bangumi_cm.plugin.core.extension

import com.heyanle.easy_bangumi_cm.plugin.api.ExtensionInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Created by heyanlin on 2024/12/9.
 */
class ExtensionController {

    data class ExtensionState(
        val loading: Boolean = true,
        val extensionInfoInfoMap: Map<String, ExtensionInfo> = emptyMap()
    )
    private val _state = MutableStateFlow<ExtensionState>(
        ExtensionState()
    )
    val state = _state.asStateFlow()


}