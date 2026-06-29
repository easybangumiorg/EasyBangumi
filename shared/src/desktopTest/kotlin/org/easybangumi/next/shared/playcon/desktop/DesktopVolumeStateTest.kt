package org.easybangumi.next.shared.playcon.desktop

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopVolumeStateTest {

    @Test
    fun clampVolumeWithinBounds() {
        assertEquals(0, DesktopVolumeState.clamp(-10))
        assertEquals(42, DesktopVolumeState.clamp(42))
        assertEquals(100, DesktopVolumeState.clamp(180))
    }

    @Test
    fun stepVolumeAlwaysClamped() {
        assertEquals(100, DesktopVolumeState.step(98, 10))
        assertEquals(0, DesktopVolumeState.step(2, -10))
        assertEquals(55, DesktopVolumeState.step(50, 5))
    }

    @Test
    fun syncSnapshotKeepsMuteAndClampsVolume() {
        val mutedSnapshot = DesktopVolumeState.sync(volume = 120, muted = true)
        assertEquals(100, mutedSnapshot.volume)
        assertEquals(true, mutedSnapshot.muted)

        val unmutedSnapshot = DesktopVolumeState.sync(volume = -8, muted = false)
        assertEquals(0, unmutedSnapshot.volume)
        assertEquals(false, unmutedSnapshot.muted)
    }
}
