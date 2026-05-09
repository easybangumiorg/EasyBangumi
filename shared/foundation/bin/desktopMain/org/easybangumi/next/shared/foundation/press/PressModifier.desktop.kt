package org.easybangumi.next.shared.foundation.press

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    onRightPress: () -> Unit,
): Modifier {
    return this
//    // 注意：onRightPress 在 expect 中有默认值，但 actual 函数不能有默认值
//    // 调用者需要通过命名参数传递
//    // 长按时间阈值（毫秒）
//    val longPressTimeMillis = 500L
//
//    return this.combinedClickable {  }.pointerInput(Unit) {
//        awaitPointerEventScope {
//            while (true) {
//                val event = awaitPointerEvent()
//
//                when (event.type) {
//                    PointerEventType.Press -> {
//                        event.changes.firstOrNull()
//                        // 检测按下的按钮
//                        when  {
//                            event.buttons.isPrimaryPressed -> {
//                                // 左键按下
//                                var isLongPressTriggered = false
//                                var longPressJob: kotlinx.coroutines.Job? = null
//
//                                // 启动长按检测任务
//                                longPressJob = launch {
//                                    delay(longPressTimeMillis)
//                                    if (isActive) {
//                                        isLongPressTriggered = true
//                                        onLongPress()
//                                    }
//                                }
//
//                                // 等待释放或取消
//                                var shouldBreak = false
//                                while (!shouldBreak) {
//                                    val nextEvent = awaitPointerEvent()
//
//                                    when (nextEvent.type) {
//                                        PointerEventType.Release -> {
//                                            // 按钮释放
//                                            longPressJob?.cancel()
//                                            if (!isLongPressTriggered) {
//                                                onPress()
//                                            }
//                                            shouldBreak = true
//                                        }
//                                        PointerEventType.Move -> {
//                                            // 检查是否还在按下状态
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
//                                            // 事件取消，取消长按检测
//                                            longPressJob?.cancel()
//                                            shouldBreak = true
//                                        }
//                                        else -> {
//                                            // 其他事件，继续等待
//                                        }
//                                    }
//                                }
//                            }
//                            event.buttons.isSecondaryPressed -> {
//                                // 右键按下，直接触发右键回调
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
//                                // 其他按钮，忽略
//                            }
//                        }
//                    }
//                    else -> {
//                        // 其他事件类型，继续循环
//                    }
//                }
//            }
//        }
//    }
}