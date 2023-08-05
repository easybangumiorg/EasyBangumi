package com.heyanle.lib_anim.utils.preference

/**
 * Created by HeYanLe on 2023/8/4 22:47.
 * https://github.com/heyanLE
 */
interface PreferenceHelper {

    fun save(key: String, value: String)

    fun load(key: String, def: String): String

}