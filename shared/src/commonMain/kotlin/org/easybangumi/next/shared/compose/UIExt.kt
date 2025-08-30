package org.easybangumi.next.shared.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.easybangumi.next.isAndroid
import org.easybangumi.next.isIos
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.UIMode
import org.easybangumi.next.shared.preference.MainPreference
import org.easybangumi.next.shared.store.collectAsState
import org.koin.compose.koinInject


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
 *
 *  UI 模式分为两个维度
 *  按窗口尺寸：小屏幕（手机、桌面窗口缩小）和大屏幕（平板、桌面窗口放大）
 *  按输入方式：触控（手机、平板、手动开启触控模式的桌面）和鼠标（桌面默认）
 */

object UI {

    @Composable
    fun getUiMode(): UIMode {
        val tabletMode = isTabletMode()
        val inputMode = getInputMode()
        return UIMode(
            isTableMode = tabletMode,
            inputMode = inputMode
        )
    }

    @Composable
    fun isTabletMode(): Boolean {
        val tabletPref = koinInject<MainPreference>().tabletMode
        val tableMode = tabletPref.collectAsState()
        val isTabletModeWhenAuto = isTabletModeWhenAuto()
        return remember(isTabletModeWhenAuto, tableMode.value) {
            when(val mode = tableMode.value) {
                MainPreference.TabletMode.AUTO -> {
                    isTabletModeWhenAuto
                }

                MainPreference.TabletMode.ENABLE -> {
                    true
                }

                MainPreference.TabletMode.DISABLE -> {
                    false
                }
            }
        }
    }

    @Composable
    fun getInputMode(): InputMode {
        val inputModelPref = koinInject<MainPreference>().inputModel
        val inputModel = inputModelPref.collectAsState()
        return when(inputModel.value) {
            MainPreference.InputModel.AUTO -> {
                // 默认模式直接按照平台区分，特殊情况（如触控桌面端）让用户手动选择
                if (platformInformation.isAndroid() || platformInformation.isIos())
                    InputMode.TOUCH
                else
                    InputMode.POINTER
            }

            MainPreference.InputModel.POINTER -> {
                InputMode.POINTER
            }

            MainPreference.InputModel.TOUCH -> {
                InputMode.TOUCH
            }
        }
    }

    @Composable
    fun isTouchMode(): Boolean {
       return getInputMode() == InputMode.TOUCH
    }

}

@Composable
internal expect fun isTabletModeWhenAuto(): Boolean



