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
class MainPreference(
    private val preferenceStore: PreferenceStore
) {

    // 大尺寸配置模式配置
    enum class TabletMode {
        AUTO, ENABLE, DISABLE
    }
    val tabletMode = preferenceStore.getEnum<TabletMode>("mobile_pad_mode", TabletMode.AUTO)


    // 输入模式配置
    // 目前不支持综合，但是鼠标可以满足所有 TOUCH 模式的需求
    enum class InputModel {
        AUTO, POINTER, TOUCH
    }
    val inputModel = preferenceStore.getEnum<InputModel>("input_model", InputModel.AUTO)

    // 私密模式，开启后播放的内容不会出现在历史记录中
    val privateMode = preferenceStore.getBoolean("private_mode", false)


}