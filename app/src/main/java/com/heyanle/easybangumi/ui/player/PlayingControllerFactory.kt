package com.heyanle.easybangumi.ui.player

import android.util.LruCache
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary

/**
 * lru 储存 AnimPlayingController
 * Created by HeYanLe on 2023/2/5 11:50.
 * https://github.com/heyanLE
 */
object PlayingControllerFactory {

    private val playItemControllerLru =
        object : LruCache<BangumiSummary, AnimPlayingController>(10) {
            override fun entryRemoved(
                evicted: Boolean,
                key: BangumiSummary?,
                oldValue: AnimPlayingController?,
                newValue: AnimPlayingController?
            ) {
                super.entryRemoved(evicted, key, oldValue, newValue)
                oldValue?.release()
            }
        }

    fun getItemController(bangumiSummary: BangumiSummary): AnimPlayingController {
        val cache = playItemControllerLru[bangumiSummary]
        if (cache != null) {
            return cache
        }
        val newValue = AnimPlayingController(bangumiSummary)
        playItemControllerLru.put(bangumiSummary, newValue)
        return newValue
    }

}