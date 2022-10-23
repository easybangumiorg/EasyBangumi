package com.heyanle.easy_player.player

/**
 * Created by HeYanLe on 2022/10/23 15:14.
 * https://github.com/heyanLE
 */
interface ProgressManager {

    fun saveProgress(key: String, progress: Long)

    fun getProgress(key: String): Long?

}