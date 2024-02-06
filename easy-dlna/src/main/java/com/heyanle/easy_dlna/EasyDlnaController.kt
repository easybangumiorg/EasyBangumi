package com.heyanle.easy_dlna

import org.cybergarage.upnp.Device

/**
 * Created by heyanlin on 2024/2/6 14:18.
 */
interface EasyDlnaController {

    fun setUrl(device: Device, url: String): Boolean

    fun play(device: Device): Boolean

    fun pause(device: Device): Boolean

    fun stop(device: Device): Boolean


    fun setMute(device: Device, isMute: Boolean): Boolean

    fun getMute(device: Device): Boolean

    fun setVolume(device: Device, volume: Int): Boolean

    fun getVolume(device: Device): Int

    fun getDuration(device: Device): String

    fun getPosition(device: Device): String

    fun getMaxVolume(device: Device): Int

    fun getMinVolume(device: Device): Int

}