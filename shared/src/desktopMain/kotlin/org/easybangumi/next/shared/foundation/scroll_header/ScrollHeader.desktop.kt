package org.easybangumi.next.shared.foundation.scroll_header

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput

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
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <SCOPE : ScrollHeaderScope> ScrollableHeaderScaffoldDesktop(
    modifier: Modifier,
    behavior: ScrollableHeaderBehavior<SCOPE>,
    nested: Boolean = true,
    content: @Composable SCOPE.() -> Unit
) {
    Box(modifier.run {
        if (nested) {
            nestedScroll(
                behavior.nestedScrollConnection,
            )
//                .onPointerEvent(PointerEventType.Scroll) {
//
//                }
                .clipToBounds()
        } else {
            this
        }
    }) {
        content.invoke(behavior.scope)
    }

}