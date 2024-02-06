package com.heyanle.easy_dlna

import org.cybergarage.upnp.Device

/**
 * Created by heyanlin on 2024/2/6 14:24.
 */
interface EasyDlnaContainer {

    fun init(): Boolean

    fun search()

    fun subscribe(device: Device): Boolean

    fun setClient(easyDlnaClient: EasyDlnaClient)

    fun getDeviceList(): List<Device>

    fun release()

}