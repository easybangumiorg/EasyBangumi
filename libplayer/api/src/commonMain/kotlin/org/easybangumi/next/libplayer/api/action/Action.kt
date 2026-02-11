package org.easybangumi.next.libplayer.api.action

import org.easybangumi.next.libplayer.api.PlayerBridge

/**
 * Created by heyanle on 2025/5/27.
 */
interface Action<B: PlayerBridge<*>> {
    fun onBind(bridge: B)
    fun onUnbind()
}