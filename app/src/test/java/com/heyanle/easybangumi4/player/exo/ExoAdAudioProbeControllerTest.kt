package com.heyanle.easybangumi4.player.exo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExoAdAudioProbeControllerTest {

    @Test
    fun stableProbeMediaId_isDeterministicAndWithinProbeLimit() {
        val longMediaId = "source:" + "episode-id".repeat(100)

        val first = stableProbeMediaId(longMediaId)
        val second = stableProbeMediaId(longMediaId)

        assertEquals(first, second)
        assertTrue(first.startsWith("easybangumi-"))
        assertTrue(first.length in 1..256)
        assertNotEquals(first, stableProbeMediaId("another-media"))
    }
}
