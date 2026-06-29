package org.easybangumi.next.shared.playcon.desktop

internal object DesktopVolumeState {
    const val MIN = 0
    const val MAX = 100

    fun clamp(volume: Int): Int = volume.coerceIn(MIN, MAX)

    fun step(current: Int, delta: Int): Int = clamp(current + delta)

    fun sync(volume: Int, muted: Boolean): Snapshot {
        return Snapshot(volume = clamp(volume), muted = muted)
    }

    data class Snapshot(
        val volume: Int,
        val muted: Boolean,
    )
}
