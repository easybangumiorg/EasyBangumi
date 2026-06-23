package com.heyanle.easybangumi4.plugin.source.js.entity

import java.util.ArrayList
import java.util.Objects

class MainTab (
    val label: String,
    val type: Int,

    // js 缁存姢鐢ㄤ互閫忎紶鏁版嵁
    val ext: Object?,
) {
    companion object {
        const val MAIN_TAB_GROUP = 0
        const val MAIN_TAB_WITH_COVER = 1
        const val MAIN_TAB_WITHOUT_COVER = 2
    }

    constructor(label: String, type: Int): this(label, type, null)
}

class NonLabelMainTab(
    val type: Int,

    // js 缁存姢鐢ㄤ互閫忎紶鏁版嵁
    val ext: Object?,
): ArrayList<MainTab>() {
    constructor(type: Int): this(type, null)
}

class SubTab (
    val label: String,
    val isCover: Boolean,

    // js 缁存姢鐢ㄤ互閫忎紶鏁版嵁
    val ext: String?,
){
    constructor(label: String, isCover: Boolean): this(label, isCover, null)
}