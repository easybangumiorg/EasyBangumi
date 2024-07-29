package com.heyanle.easybangumi4.plugin.js.entity

data class MainTab (
    val label: String,
    val type: Int,
) {
    companion object {
        const val MAIN_TAB_GROUP = 0
        const val MAIN_TAB_WITH_COVER = 1
        const val MAIN_TAB_WITHOUT_COVER = 2
    }
}

data class SubTab (
    val label: String,
    val isCover: Boolean,
)