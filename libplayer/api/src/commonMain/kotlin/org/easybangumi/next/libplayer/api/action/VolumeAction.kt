package org.easybangumi.next.libplayer.api.action

import org.easybangumi.next.libplayer.api.PlayerBridge

/**
 * Desktop audio control action abstraction.
 */
interface VolumeAction<B : PlayerBridge<*>> : Action<B> {
    fun getVolume(): VolumeActionResult<Int>
    fun setVolume(volume: Int): VolumeActionResult<Int>
    fun isMuted(): VolumeActionResult<Boolean>
    fun setMuted(muted: Boolean): VolumeActionResult<Boolean>
}

sealed class VolumeActionResult<out T> {
    data class Success<T>(val value: T) : VolumeActionResult<T>()
    data class Failure(val reason: Reason) : VolumeActionResult<Nothing>()

    enum class Reason {
        UNSUPPORTED,
        BRIDGE_NOT_READY,
        EXECUTION_FAILED,
    }
}
