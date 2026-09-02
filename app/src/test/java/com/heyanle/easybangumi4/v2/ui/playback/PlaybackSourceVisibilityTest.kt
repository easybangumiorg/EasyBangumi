package com.heyanle.easybangumi4.v2.ui.playback

import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSourceVisibilityTest {

    @Test
    fun localSourceDoesNotShowPlaybackSourceEntry() {
        assertFalse(shouldShowPlaybackSource(LocalSource.LOCAL_SOURCE_KEY))
    }

    @Test
    fun onlineSourceShowsPlaybackSourceEntry() {
        assertTrue(shouldShowPlaybackSource("online-source"))
    }
}
