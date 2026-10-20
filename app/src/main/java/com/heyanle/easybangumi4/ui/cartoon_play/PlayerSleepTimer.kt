package com.heyanle.easybangumi4.ui.cartoon_play

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PlayerSleepTimerOption(val minutes: Int? = null) {
    OFF,
    FIFTEEN_MINUTES(15),
    THIRTY_MINUTES(30),
    SIXTY_MINUTES(60),
    END_OF_EPISODE,
}

data class PlayerSleepTimerState(
    val option: PlayerSleepTimerOption = PlayerSleepTimerOption.OFF,
    val remainingMs: Long = 0L,
    val stoppedByTimer: Boolean = false,
)

/**
 * A timer for one playback session. Call from the player/main dispatcher, [clear] when leaving
 * the player, and [onEpisodeEnded] before attempting automatic next-episode playback.
 *
 * A monotonic deadline keeps the countdown correct after buffering, backgrounding, or device
 * sleep. No preference is saved and manual pauses do not extend the chosen duration.
 */
class PlayerSleepTimer(
    private val scope: CoroutineScope,
    private val elapsedRealtimeMs: () -> Long = SystemClock::elapsedRealtime,
    private val onTimerElapsed: () -> Unit,
) {
    private val mutableState = MutableStateFlow(PlayerSleepTimerState())
    val state: StateFlow<PlayerSleepTimerState> = mutableState.asStateFlow()

    private var countdownJob: Job? = null
    private var deadlineMs: Long? = null

    fun select(option: PlayerSleepTimerOption) {
        clear()
        val durationMs = option.minutes?.times(60_000L)
        mutableState.value = PlayerSleepTimerState(option, durationMs ?: 0L)
        if (durationMs == null) return

        deadlineMs = elapsedRealtimeMs() + durationMs
        countdownJob = scope.launch {
            while (true) {
                val remainingMs = ((deadlineMs ?: return@launch) - elapsedRealtimeMs())
                    .coerceAtLeast(0L)
                if (remainingMs == 0L) {
                    finish()
                    return@launch
                }
                mutableState.value = mutableState.value.copy(remainingMs = remainingMs)
                delay(minOf(1_000L, remainingMs))
            }
        }
    }

    /** Returns true when the caller must skip automatic next-episode playback. */
    fun onEpisodeEnded(): Boolean {
        val current = mutableState.value
        if (current.stoppedByTimer) return true
        if (current.option == PlayerSleepTimerOption.END_OF_EPISODE ||
            deadlineMs?.let { elapsedRealtimeMs() >= it } == true
        ) {
            finish()
            return true
        }
        return false
    }

    /** Call on a new playWhenReady=true event so an explicit resume re-enables auto-next. */
    fun onPlaybackStarted() {
        if (mutableState.value.stoppedByTimer) mutableState.value = PlayerSleepTimerState()
    }

    fun clear() {
        countdownJob?.cancel()
        countdownJob = null
        deadlineMs = null
        mutableState.value = PlayerSleepTimerState()
    }

    private fun finish() {
        clear()
        // Keep auto-next blocked if ENDED arrives just after the countdown pauses the player.
        mutableState.value = PlayerSleepTimerState(stoppedByTimer = true)
        onTimerElapsed()
    }
}
