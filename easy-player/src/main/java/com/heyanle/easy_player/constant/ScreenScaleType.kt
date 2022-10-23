package com.heyanle.easy_player.constant

/**
 * Created by HeYanLe on 2022/10/23 15:29.
 * https://github.com/heyanLE
 */
object ScreenScaleType {
    const val SCREEN_SCALE_DEFAULT = 0          // 默认
    const val SCREEN_SCALE_16_9 = 1             // 16/9
    const val SCREEN_SCALE_4_3 = 2              // 4/3
    const val SCREEN_SCALE_MATCH_PARENT = 3     // 拉伸
    const val SCREEN_SCALE_ORIGINAL = 4         // 平铺
    const val SCREEN_SCALE_CENTER_CROP = 5      // 平铺，从中心裁切，保证占满屏幕
}