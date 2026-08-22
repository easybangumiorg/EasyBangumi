package com.heyanle.easybangumi4.anime4k

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Anime4KPlaybackControllerTest {

    @Test
    fun disabledConfigsCollapseToOneEffectiveConfig() {
        assertEquals(
            Anime4KPlaybackConfig.Disabled,
            Anime4KPlaybackConfig(
                enabled = false,
                mode = 6,
                quality = A4KChain.QUALITY_L,
                scale = 4,
            ).normalized(),
        )
    }

    @Test
    fun invalidEnabledConfigFallsBackToSafeDefaults() {
        assertEquals(
            Anime4KPlaybackConfig(
                enabled = true,
                mode = A4KChain.DEFAULT_MODE,
                quality = A4KChain.DEFAULT_QUALITY,
                scale = 0,
            ),
            Anime4KPlaybackConfig(
                enabled = true,
                mode = -1,
                quality = "invalid",
                scale = 3,
            ).normalized(),
        )
    }

    @Test
    fun videoEffectErrorCodesAreSeparatedFromMediaErrors() {
        assertTrue(
            Anime4KPlaybackController.isVideoEffectErrorCode(
                PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSOR_INIT_FAILED,
            ),
        )
        assertTrue(
            Anime4KPlaybackController.isVideoEffectErrorCode(
                PlaybackException.ERROR_CODE_VIDEO_FRAME_PROCESSING_FAILED,
            ),
        )
        assertFalse(
            Anime4KPlaybackController.isVideoEffectErrorCode(
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            ),
        )
    }
}
