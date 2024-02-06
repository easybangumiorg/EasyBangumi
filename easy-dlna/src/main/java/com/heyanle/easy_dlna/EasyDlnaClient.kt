package com.heyanle.easy_dlna

import org.cybergarage.upnp.Device
import org.cybergarage.upnp.ssdp.SSDPPacket

/**
 * Created by heyanlin on 2024/2/6 14:25.
 */
interface EasyDlnaClient {

    fun onDeviceListChange(device: List<Device>)

    fun onEventNotify(uuid: String, sql:Long, name: String, value: String) {

    }




}