package org.easybangumi.next.shared.foundation.press

import androidx.compose.ui.Modifier

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
expect fun Modifier.PressModifier(
    onPress: () -> Unit,
    // 长按
    onLongPress: () -> Unit,
    // 鼠标右键， 默认触发长按行为
    onRightPress: () -> Unit = {
        onLongPress()
    },
): Modifier