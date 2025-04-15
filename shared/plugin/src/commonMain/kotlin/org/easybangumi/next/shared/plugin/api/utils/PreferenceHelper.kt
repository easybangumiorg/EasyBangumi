package org.easybangumi.next.shared.plugin.api.utils


/**
 * Created by HeYanLe on 2024/12/8 23:04.
 * https://github.com/heyanLE
 */

interface PreferenceHelper {

    /**
     * 获取所有存储的键值对（无序）
     */
    fun map(): Map<String, String>

    fun get(key: String, def: String): String

    fun put(key: String, value: String)

}