package org.easybangumi.next.shared.foundation

import androidx.compose.runtime.compositionLocalOf

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
enum class InputMode {
    POINTER,
    TOUCH,
}
data class UIMode (
    val isTableMode: Boolean,
    val inputMode: InputMode,
)
val LocalUIMode = compositionLocalOf<UIMode> {
    error("No UIMode provided")
}
