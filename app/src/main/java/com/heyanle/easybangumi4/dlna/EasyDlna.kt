package com.heyanle.easybangumi4.dlna

import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
) {

    private val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _deviceList = MutableStateFlow<List<Device>>(emptyList())
    val deviceList = _deviceList.asStateFlow()


    init {
        controlPoint.addEventListener { uuid, seq, varName, value ->
            //client?.onEventNotify(uuid, seq, varName, value)
        }
        controlPoint.addDeviceChangeListener(object : DeviceChangeListener {
            override fun deviceAdded(dev: Device?) {
                if (!DLNAUtils.isMediaRenderDevice(dev)) {
                    return
                }
                dev ?: return
                _deviceList.update {
                    val o = it.find { it.udn.uppercase() == dev.udn.uppercase() }
                    if (o == null) {
                        it + dev
                    } else {
                        it.map { if (it == o) dev else o }
                    }
                }
            }

            override fun deviceRemoved(dev: Device?) {
                if (!DLNAUtils.isMediaRenderDevice(dev)) {
                    return
                }
                dev ?: return
                _deviceList.update {
                    it.flatMap {
                        if (it.udn.uppercase() == dev.udn.uppercase()) {
                            emptyList()
                        } else {
                            listOf(it)
                        }
                    }
                }
            }
        })
    }

    suspend fun init(): Boolean {
        return scope.async {
            controlPoint.start()
        }.await()
    }

    suspend fun search() {
        scope.async {
            controlPoint.search()
        }.await()
    }

    suspend fun subscribe(device: Device): Boolean {
        return scope.async {
            val service = device.getService(DLNAUtils.AVTransport1) ?: return@async false
            return@async controlPoint.subscribe(service)
        }.await()
    }


    fun release() {
        controlPoint.stop()
    }


    suspend fun setUrl(device: Device, url: String): Boolean {
        if (url.isEmpty()) {
            return false
        }

        return scope.async {
            device.postAction(DLNAUtils.ActionSetAVTransportURI) {
                setArgumentValue("CurrentURI", url)
                setArgumentValue("CurrentURIMetaData", 0)
            }
        }.await()
    }

    suspend fun play(device: Device): Boolean {
        return scope.async {
            device.postAction(DLNAUtils.ActionPlay) {
                setArgumentValue("Speed", "1")
            }
        }.await()
    }

    suspend fun pause(device: Device): Boolean {
        return scope.async {
            device.postAction(DLNAUtils.ActionPause)
        }.await()
    }

    suspend fun stop(device: Device): Boolean {
        return scope.async {
            device.postAction(DLNAUtils.ActionStop)
        }.await()
    }

    suspend fun setMute(device: Device, isMute: Boolean): Boolean {
        return scope.async {
            device.postAction(DLNAUtils.ActionSetMute) {
                setArgumentValue("Channel", "Master")
                setArgumentValue("DesiredMute", if (isMute) "1" else "0")
            }
        }.await()
    }

    suspend fun getMute(device: Device): Boolean {
        return scope.async {
            device.postActionArgument(DLNAUtils.ActionGetMute, "CurrentMute") == "1"
        }.await()
    }

    suspend fun setVolume(device: Device, volume: Int): Boolean {
        return scope.async {
            device.postAction(DLNAUtils.ActionSetVolume) {
                setArgumentValue("Channel", "Master")
                setArgumentValue("DesiredVolume", volume)
            }
        }.await()
    }

    suspend fun getVolume(device: Device): Int {
        return scope.async {
            device.postActionArgument(DLNAUtils.ActionGetVolume, "CurrentVolume").toIntOrNull()
                ?: -1
        }.await()
    }

    suspend fun getDuration(device: Device): String {
        return scope.async {
            device.postActionArgument(DLNAUtils.ActionGetMediaInfo, "MediaDuration")
        }.await()
    }

    suspend fun getPosition(device: Device): String {
        return scope.async {
            device.postActionArgument(DLNAUtils.ActionGetPosition, "AbsTime")
        }.await()
    }

    suspend fun getMaxVolume(device: Device): Int {
        return scope.async {
            device.postActionArgument(DLNAUtils.ActionGetVolumeRange, "MaxValue").toIntOrNull()
                ?: -1
        }.await()
    }

    suspend fun getMinVolume(device: Device): Int {
        return scope.async {
            device.postActionArgument(DLNAUtils.ActionGetVolumeRange, "MinValue").toIntOrNull()
                ?: -1
        }.await()
    }

    private inline fun Device.postAction(act: String, block: Action.() -> Unit = {}): Boolean {
        val service = getService(DLNAUtils.AVTransport1) ?: return false
        val action = service.getAction(act) ?: return false
        action.setArgumentValue("InstanceID", 0)
        action.block()
        return action.postControlAction()
    }

    private inline fun Device.postActionArgument(
        act: String,
        arg: String,
        def: String = "",
        block: Action.() -> Unit = {}
    ): String {
        val service = getService(DLNAUtils.AVTransport1) ?: return ""
        val action = service.getAction(act) ?: return ""
        action.setArgumentValue("InstanceID", 0)
        action.block()
        if (action.postControlAction()) {
            return action.getArgumentValue(arg) ?: ""
        }
        return def
    }

}