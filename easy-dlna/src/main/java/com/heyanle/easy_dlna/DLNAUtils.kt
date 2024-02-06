package com.heyanle.easy_dlna

import org.cybergarage.upnp.Device

/**
 * Created by heyanlin on 2024/2/6 14:35.
 */
object DLNAUtils {

    const val Renderer = "urn:schemas-upnp-org:device:MediaRenderer:1"

    const val AVTransport1 = "urn:schemas-upnp-org:service:AVTransport:1"
    const val RenderingControl = "urn:schemas-upnp-org:service:RenderingControl:1"


    const val ActionSetAVTransportURI = "SetAVTransportURI"
    const val ActionPlay = "Play"
    const val ActionPause = "Pause"
    const val ActionStop = "Stop"
    const val ActionGetVolume = "GetVolume"
    const val ActionGetVolumeRange = "GetVolumeDBRange"
    const val ActionSetVolume = "SetVolume"
    const val ActionGetMute = "GetMute"
    const val ActionSetMute = "SetMute"
    const val ActionGetPosition = "GetPositionInfo"
    const val ActionSeek = "Seek"
    const val ActionGetMediaInfo = "GetMediaInfo"


    fun isMediaRenderDevice(device: Device?): Boolean {
        device ?: return false
        return device.deviceType == Renderer
    }

}