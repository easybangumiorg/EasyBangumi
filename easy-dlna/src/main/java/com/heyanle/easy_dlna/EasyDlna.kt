package com.heyanle.easy_dlna

import org.cybergarage.upnp.Action
import org.cybergarage.upnp.ControlPoint
import org.cybergarage.upnp.Device
import org.cybergarage.upnp.device.DeviceChangeListener
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by heyanlin on 2024/2/6 14:23.
 */
class EasyDlna(
    private val controlPoint: ControlPoint
) : EasyDlnaController, EasyDlnaContainer {

    private val deviceList = CopyOnWriteArrayList<Device>()
    private var client: EasyDlnaClient? = null

    init {
        controlPoint.addEventListener { uuid, seq, varName, value ->
            client?.onEventNotify(uuid, seq, varName, value)
        }
        controlPoint.addDeviceChangeListener(object : DeviceChangeListener {
            override fun deviceAdded(dev: Device?) {
                if (!DLNAUtils.isMediaRenderDevice(dev)) {
                    return
                }
                dev ?: return
                val old = deviceList.find { it.udn.uppercase() == dev.udn.uppercase() }
                if (old != null) {
                    deviceList.remove(old)
                    deviceList.add(dev)
                    client?.onDeviceListChange(deviceList.toList())
                }
            }

            override fun deviceRemoved(dev: Device?) {
                if (!DLNAUtils.isMediaRenderDevice(dev)) {
                    return
                }
                dev ?: return
                val old = deviceList.find { it.udn.uppercase() == dev.udn.uppercase() } ?: return
                deviceList.remove(old)
                client?.onDeviceListChange(deviceList.toList())
            }
        })
    }

    override fun init(): Boolean {

        return controlPoint.start()
    }

    override fun search() {
        controlPoint.search()
    }

    override fun subscribe(device: Device): Boolean {
        val service = device.getService(DLNAUtils.AVTransport1) ?: return false
        return controlPoint.subscribe(service)
    }

    override fun setClient(easyDlnaClient: EasyDlnaClient) {
        this.client = easyDlnaClient
    }

    override fun getDeviceList(): List<Device> {
        return deviceList.toList()
    }

    override fun release() {
        controlPoint.stop()
    }


    override fun setUrl(device: Device, url: String): Boolean {
        if (url.isEmpty()) {
            return false
        }
        return device.postAction(DLNAUtils.ActionSetAVTransportURI) {
            setArgumentValue("CurrentURI", url)
            setArgumentValue("CurrentURIMetaData", 0)
        }
    }

    override fun play(device: Device): Boolean {
        return device.postAction(DLNAUtils.ActionPlay) {
            setArgumentValue("Speed", "1")
        }
    }

    override fun pause(device: Device): Boolean {
        return device.postAction(DLNAUtils.ActionPause)
    }

    override fun stop(device: Device): Boolean {
        return device.postAction(DLNAUtils.ActionStop)
    }

    override fun setMute(device: Device, isMute: Boolean): Boolean {
        return device.postAction(DLNAUtils.ActionSetMute) {
            setArgumentValue("Channel", "Master")
            setArgumentValue("DesiredMute", if (isMute) "1" else "0")
        }
    }

    override fun getMute(device: Device): Boolean {
        return device.postActionArgument(DLNAUtils.ActionGetMute, "CurrentMute") == "1"
    }

    override fun setVolume(device: Device, volume: Int): Boolean {
        return device.postAction(DLNAUtils.ActionSetVolume) {
            setArgumentValue("Channel", "Master")
            setArgumentValue("DesiredVolume", volume)
        }
    }

    override fun getVolume(device: Device): Int {
        return device.postActionArgument(DLNAUtils.ActionGetVolume, "CurrentVolume").toIntOrNull() ?: -1
    }

    override fun getDuration(device: Device): String {
        return device.postActionArgument(DLNAUtils.ActionGetMediaInfo, "MediaDuration")
    }

    override fun getPosition(device: Device): String {
        return device.postActionArgument(DLNAUtils.ActionGetPosition, "AbsTime")
    }

    override fun getMaxVolume(device: Device): Int {
        return device.postActionArgument(DLNAUtils.ActionGetVolumeRange, "MaxValue").toIntOrNull() ?: -1
    }

    override fun getMinVolume(device: Device): Int {
        return device.postActionArgument(DLNAUtils.ActionGetVolumeRange, "MinValue").toIntOrNull() ?: -1
    }

    private inline fun Device.postAction(act: String, block: Action.() -> Unit = {}): Boolean {
        val service = getService(DLNAUtils.AVTransport1) ?: return false
        val action = service.getAction(act) ?: return false
        action.setArgumentValue("InstanceID", 0)
        action.block()
        return action.postControlAction()
    }

    private inline fun Device.postActionArgument(act: String, arg: String, def: String = "", block: Action.() -> Unit = {}) : String {
        val service = getService(DLNAUtils.AVTransport1) ?: return ""
        val action = service.getAction(act) ?: return ""
        action.setArgumentValue("InstanceID", 0)
        action.block()
        if(action.postControlAction()){
            return action.getArgumentValue(arg) ?: ""
        }
        return def
    }

}