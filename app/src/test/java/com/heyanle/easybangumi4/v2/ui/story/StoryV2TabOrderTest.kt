package com.heyanle.easybangumi4.v2.ui.story

import org.junit.Assert.assertEquals
import org.junit.Test

class StoryV2TabOrderTest {

    @Test
    fun localCacheIsTheSecondTabAndDownloadsAreLast() {
        assertEquals(listOf("本地番剧", "本地缓存", "下载任务"), STORY_V2_TAB_LABELS)
    }
}
