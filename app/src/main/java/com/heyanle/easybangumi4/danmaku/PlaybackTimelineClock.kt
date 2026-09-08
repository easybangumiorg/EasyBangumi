package com.heyanle.easybangumi4.danmaku

/**
 * Thread-safe media clock shared by the player thread and DFM's drawing thread.
 *
 * Player position is the source of truth. Between snapshots we interpolate with playback speed so
 * scrolling remains smooth even when the player reports position less frequently than DFM draws.
 */
internal class PlaybackTimelineClock {
    private var snapshot = Snapshot()
    private var lastPositionMillis = 0L

    @Synchronized
    fun synchronize(
        positionMillis: Long,
        playbackSpeed: Float,
        isPlaying: Boolean,
        realtimeMillis: Long,
        allowBackward: Boolean = false,
    ) {
        val position = positionMillis.coerceAtLeast(0L)
        snapshot = Snapshot(
            positionMillis = position,
            realtimeMillis = realtimeMillis,
            playbackSpeed = playbackSpeed.normalizedPlaybackSpeed(),
            isPlaying = isPlaying,
        )
        if (allowBackward || !isPlaying) {
            lastPositionMillis = position
        }
    }

    @Synchronized
    fun positionAt(realtimeMillis: Long): Long {
        val elapsedRealtime = (realtimeMillis - snapshot.realtimeMillis).coerceAtLeast(0L)
        val interpolated = if (snapshot.isPlaying) {
            snapshot.positionMillis + (elapsedRealtime * snapshot.playbackSpeed).toLong()
        } else {
            snapshot.positionMillis
        }.coerceAtLeast(0L)
        // A small negative correction freezes briefly instead of moving visible comments right.
        // Explicit seeks reset this floor via allowBackward and may intentionally move backwards.
        lastPositionMillis = maxOf(lastPositionMillis, interpolated)
        return lastPositionMillis
    }

    private data class Snapshot(
        val positionMillis: Long = 0L,
        val realtimeMillis: Long = 0L,
        val playbackSpeed: Float = 1f,
        val isPlaying: Boolean = false,
    )
}

internal fun Float.normalizedPlaybackSpeed(): Float =
    takeIf { it.isFinite() && it > 0f }?.coerceIn(0.25f, 4f) ?: 1f
