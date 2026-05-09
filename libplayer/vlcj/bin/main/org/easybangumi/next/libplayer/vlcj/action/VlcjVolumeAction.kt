package org.easybangumi.next.libplayer.vlcj.action

import org.easybangumi.next.libplayer.api.action.VolumeAction
import org.easybangumi.next.libplayer.api.action.VolumeActionResult
import org.easybangumi.next.libplayer.vlcj.BaseVlcjPlayerBridge

class VlcjVolumeAction : VolumeAction<BaseVlcjPlayerBridge> {

    private var bridge: BaseVlcjPlayerBridge? = null

    override fun getVolume(): VolumeActionResult<Int> {
        val bridge = bridge ?: return VolumeActionResult.Failure(VolumeActionResult.Reason.BRIDGE_NOT_READY)
        return runCatching {
            bridge.impl.audio().volume().coerceIn(0, 100)
        }.fold(
            onSuccess = { VolumeActionResult.Success(it) },
            onFailure = {
                it.printStackTrace()
                VolumeActionResult.Failure(VolumeActionResult.Reason.EXECUTION_FAILED) }
        )
    }

    override fun setVolume(volume: Int): VolumeActionResult<Int> {
        val bridge = bridge ?: return VolumeActionResult.Failure(VolumeActionResult.Reason.BRIDGE_NOT_READY)
        val target = volume.coerceIn(0, 100)
        return runCatching {
            val updated = bridge.impl.audio().setVolume(target)
            if (updated) {
                VolumeActionResult.Success(target)
            } else {
                VolumeActionResult.Failure(VolumeActionResult.Reason.UNSUPPORTED)
            }
        }.getOrElse {
            VolumeActionResult.Failure(VolumeActionResult.Reason.EXECUTION_FAILED)
        }
    }

    override fun isMuted(): VolumeActionResult<Boolean> {
        val bridge = bridge ?: return VolumeActionResult.Failure(VolumeActionResult.Reason.BRIDGE_NOT_READY)
        return runCatching {
            bridge.impl.audio().isMute()
        }.fold(
            onSuccess = { VolumeActionResult.Success(it) },
            onFailure = { VolumeActionResult.Failure(VolumeActionResult.Reason.EXECUTION_FAILED) }
        )
    }

    override fun setMuted(muted: Boolean): VolumeActionResult<Boolean> {
        val bridge = bridge ?: return VolumeActionResult.Failure(VolumeActionResult.Reason.BRIDGE_NOT_READY)
        return runCatching {
            bridge.impl.audio().setMute(muted)
            VolumeActionResult.Success(bridge.impl.audio().isMute())
        }.getOrElse {
            VolumeActionResult.Failure(VolumeActionResult.Reason.EXECUTION_FAILED)
        }
    }

    override fun onBind(bridge: BaseVlcjPlayerBridge) {
        this.bridge = bridge
    }

    override fun onUnbind() {
        bridge = null
    }
}
