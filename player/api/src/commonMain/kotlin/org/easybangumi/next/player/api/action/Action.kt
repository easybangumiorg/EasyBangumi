package org.easybangumi.next.player.api.action

import org.easybangumi.next.player.api.PlayerBridge

/**
 * Created by heyanlin on 2025/5/27.
 */
interface Action {
    fun onBind(bridge: PlayerBridge)
    fun onUnbind(bridge: PlayerBridge)
}