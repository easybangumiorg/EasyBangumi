package org.easybangumi.next.shared.preference

import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.store.preference.getEnum

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
class AndroidPlayerPreference(
    private val preferenceStore: PreferenceStore
) {

    enum class AutoFullScreeMode {
        // 跟随用户设置
        AUTO,
        // 开启
        ENABLE,
        // 关闭
        DISABLE
    }
    // 是否根据传感器自动横屏 - 只影响横竖屏切换，横屏方向依然跟随传感器
    val autoFullScreenMode = preferenceStore.getEnum<AutoFullScreeMode>("auto_full_screen_mode", AutoFullScreeMode.AUTO)

}