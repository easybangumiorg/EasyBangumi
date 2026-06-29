package org.easybangumi.next.shared.source.api.utils


/**
 * Created by HeYanLe on 2024/12/8 23:04.
 * https://github.com/heyanLE
 */

interface PreferenceHelper {

    /**
     * 获取所有存储的键值对（无序）
     */
    suspend fun map(): Map<String, String>

    suspend fun get(key: String, def: String): String

    suspend fun put(key: String, value: String)

}