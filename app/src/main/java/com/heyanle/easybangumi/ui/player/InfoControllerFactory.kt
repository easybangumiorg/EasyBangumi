package com.heyanle.easybangumi.ui.player

import android.util.LruCache
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary

/**
 * Created by HeYanLe on 2023/2/11 21:54.
 * https://github.com/heyanLE
 */
object InfoControllerFactory {

    private val playItemControllerLru =
        object : LruCache<BangumiSummary, BangumiInfoController>(10) {
            override fun entryRemoved(
                evicted: Boolean,
                key: BangumiSummary?,
                oldValue: BangumiInfoController?,
                newValue: BangumiInfoController?
            ) {
                super.entryRemoved(evicted, key, oldValue, newValue)
                oldValue?.release()
            }
        }

    fun getInfoController(bangumiSummary: BangumiSummary): BangumiInfoController {
        val cache = playItemControllerLru[bangumiSummary]
        if (cache != null) {
            return cache
        }
        val newValue = BangumiInfoController(bangumiSummary)
        playItemControllerLru.put(bangumiSummary, newValue)
        return newValue
    }
}