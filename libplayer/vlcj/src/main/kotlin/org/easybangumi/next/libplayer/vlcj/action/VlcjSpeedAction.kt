package org.easybangumi.next.libplayer.vlcj.action

import org.easybangumi.next.libplayer.api.action.SpeedAction
import org.easybangumi.next.libplayer.vlcj.VlcjPlayerBridge

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class VlcjSpeedAction: SpeedAction<VlcjPlayerBridge> {

    private var bridge: VlcjPlayerBridge? = null
    private var speed: Float = -1f
    private var realSpeed: Float = -1f

    override fun setSpeed(speed: Float): Boolean {
        runCatching {
            this.speed = speed

            if (bridge?.impl?.controls()?.setRate(speed) == true) {
                realSpeed = speed
                return true
            }
        }.onFailure {
            it.printStackTrace()
        }
        return false
    }

    override fun getSpeed(): Float {
        return runCatching {
            bridge?.impl?.status()?.rate()
        }.getOrNull()?.also { realSpeed = it ?: -1f } ?: -1f
    }

    override fun onBind(bridge: VlcjPlayerBridge) {
        this.bridge = bridge
         if (speed > 0) {
             if (bridge.impl.controls().setRate(speed)) {
                 realSpeed = speed
             }
         }
    }

    override fun onUnbind() {
        this.bridge = null
    }
}