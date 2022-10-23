package com.heyanle.easy_player.player

import androidx.collection.LruCache

/**
 * Created by HeYanLe on 2022/10/23 15:31.
 * https://github.com/heyanLE
 */
class LruMemoryProgressManager: ProgressManager {

    private val lruCache = LruCache<String, Long>(100)

    override fun saveProgress(key: String, progress: Long) {
        lruCache.put(key, progress)
    }

    override fun getProgress(key: String): Long? {
        return lruCache.get(key)
    }
}