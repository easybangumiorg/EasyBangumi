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

actual fun Modifier.PressModifier(
    onPress: () -> Unit,
    onLongPress: () -> Unit,
    onRightPress: () -> Unit
): Modifier {
    return this
//    // 注意：onRightPress 在 expect 中有默认值，但 actual 函数不能有默认值
//    // 调用者需要通过命名参数传递
//    // Android 上使用 clickable + pointerInput 的组合方式
//    // 长按时间阈值（毫秒）
//    val longPressTimeMillis = 500L
//
//    return this.pointerInput(Unit) {
//        awaitPointerEventScope {
//            while (true) {
//                val event = awaitPointerEvent()
//
//                when (event.type) {
//                    PointerEventType.Press -> {
//                        val change = event.changes.firstOrNull() ?: continue
//                        val button = change.pressedButtons.firstOrNull()
//
//                        when (button) {
//                            PointerButton.Primary -> {
//                                // 左键按下（或触摸）
//                                var isLongPressTriggered = false
//                                var longPressJob: kotlinx.coroutines.Job? = null
//
//                                // 启动长按检测任务
//                                longPressJob = kotlinx.coroutines.launch {
//                                    kotlinx.coroutines.delay(longPressTimeMillis)
//                                    if (isActive) {
//                                        isLongPressTriggered = true
//                                        onLongPress()
//                                    }
//                                }
//
//                                // 等待释放
//                                var shouldBreak = false
//                                while (!shouldBreak) {
//                                    val nextEvent = awaitPointerEvent()
//
//                                    when (nextEvent.type) {
//                                        PointerEventType.Release -> {
//                                            longPressJob?.cancel()
//                                            if (!isLongPressTriggered) {
//                                                onPress()
//                                            }
//                                            shouldBreak = true
//                                        }
//                                        PointerEventType.Move -> {
//                                            val stillPressed = nextEvent.changes.firstOrNull()
//                                                ?.pressedButtons
//                                                ?.contains(PointerButton.Primary) ?: false
//                                            if (!stillPressed) {
//                                                longPressJob?.cancel()
//                                                if (!isLongPressTriggered) {
//                                                    onPress()
//                                                }
//                                                shouldBreak = true
//                                            }
//                                        }
//                                        PointerEventType.Cancel -> {
//                                            longPressJob?.cancel()
//                                            shouldBreak = true
//                                        }
//                                        else -> {
//                                            // 其他事件，继续等待
//                                        }
//                                    }
//                                }
//                            }
//                            PointerButton.Secondary -> {
//                                // 右键（可能是鼠标右键或辅助点击）
//                                onRightPress()
//
//                                // 等待释放
//                                while (true) {
//                                    val nextEvent = awaitPointerEvent()
//                                    if (nextEvent.type == PointerEventType.Release) {
//                                        val stillPressed = nextEvent.changes.firstOrNull()
//                                            ?.pressedButtons
//                                            ?.contains(PointerButton.Secondary) ?: false
//                                        if (!stillPressed) {
//                                            break
//                                        }
//                                    }
//                                }
//                            }
//                            else -> {
//                                // 其他按钮
//                            }
//                        }
//                    }
//                    else -> {
//                        // 其他事件类型
//                    }
//                }
//            }
//        }
//    }
}