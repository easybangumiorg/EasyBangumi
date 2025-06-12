package org.easybangumi.next.libplayer.api.action

import org.easybangumi.next.libplayer.api.PlayerBridge

/**
 * Created by heyanlin on 2025/5/27.
 */
interface Action {
    fun onBind(bridge: PlayerBridge)
    fun onUnbind(bridge: PlayerBridge)
}