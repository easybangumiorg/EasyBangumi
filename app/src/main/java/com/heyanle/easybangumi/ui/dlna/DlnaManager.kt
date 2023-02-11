package com.heyanle.easybangumi.ui.dlna

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.utils.stringRes
import com.zane.androidupnpdemo.Intents
import com.zane.androidupnpdemo.Intents.ACTION_PLAYING
import com.zane.androidupnpdemo.control.ClingPlayControl
import com.zane.androidupnpdemo.control.callback.ControlCallback
import com.zane.androidupnpdemo.entity.ClingDevice
import com.zane.androidupnpdemo.entity.ClingDeviceList
import com.zane.androidupnpdemo.entity.IDevice
import com.zane.androidupnpdemo.entity.IResponse
import com.zane.androidupnpdemo.listener.BrowseRegistryListener
import com.zane.androidupnpdemo.listener.DeviceListChangedListener
import com.zane.androidupnpdemo.service.ClingUpnpService
import com.zane.androidupnpdemo.service.manager.ClingManager
import com.zane.androidupnpdemo.service.manager.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by HeYanLe on 2023/2/11 22:11.
 * https://github.com/heyanLE
 */
object DlnaManager {

    const val TAG = "DlnaManager"

    var scope: CoroutineScope = MainScope()

    val dmrDevices = mutableStateListOf<ClingDevice>()

    val curDevice = mutableStateOf<ClingDevice?>(null)

    val isInit = AtomicBoolean(false)


    /**
     * 投屏控制器
     */
    private val mClingPlayControl: ClingPlayControl = ClingPlayControl()

    private val mTransportStateBroadcastReceiver: BroadcastReceiver =
        TransportStateBroadcastReceiver().apply {

        }

    init {
        val filter = IntentFilter()
        filter.addAction(ACTION_PLAYING)
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK)
        filter.addAction(Intents.ACTION_STOPPED)
        filter.addAction(Intents.ACTION_TRANSITIONING)
        BangumiApp.INSTANCE.registerReceiver(mTransportStateBroadcastReceiver, filter)
    }

    /** 用于监听发现设备  */
    private val mBrowseRegistryListener = BrowseRegistryListener().apply {
        this.setOnDeviceListChangedListener(object : DeviceListChangedListener {
            override fun onDeviceAdded(device: IDevice<*>?) {
                (device as? ClingDevice)?.let {
                    dmrDevices += it
                }

            }

            override fun onDeviceRemoved(device: IDevice<*>?) {
                (device as? ClingDevice)?.let {
                    dmrDevices.remove(it)
                }
            }
        })
    }

    private val mUpnpServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected")
            val binder = service as ClingUpnpService.LocalBinder
            val beyondUpnpService: ClingUpnpService = binder.service
            val clingUpnpServiceManager = ClingManager.getInstance()
            clingUpnpServiceManager.setUpnpService(beyondUpnpService)
            clingUpnpServiceManager.setDeviceManager(DeviceManager())
            clingUpnpServiceManager.registry.addListener(mBrowseRegistryListener)
            //Search on service created.
            clingUpnpServiceManager.searchDevices()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected")
            ClingManager.getInstance().setUpnpService(null)
        }
    }

    private fun initIfNeed() {
        if (isInit.compareAndSet(false, true)) {
            scope.launch {
                Log.d(TAG, "init")
                val upnpServiceIntent = Intent(BangumiApp.INSTANCE, ClingUpnpService::class.java)
                BangumiApp.INSTANCE.startService(upnpServiceIntent)
                BangumiApp.INSTANCE.bindService(
                    upnpServiceIntent,
                    mUpnpServiceConnection,
                    Context.BIND_AUTO_CREATE
                )

            }
        }
    }

    fun refresh() {
        initIfNeed()
        scope.launch {
            val devices = ClingManager.getInstance().dmrDevices
            ClingDeviceList.getInstance().clingDeviceList = devices
            dmrDevices.clear()
            if (devices != null) {
                dmrDevices.addAll(devices)
            }
        }

    }

    fun release() {
        val intent = Intent()
        intent.action = "ITOP.MOBILE.SIMPLE.SERVICE.SENSORSERVICE"
        BangumiApp.INSTANCE.stopService(intent)
        isInit.set(false)
        curDevice.value = null
        dmrDevices.clear()
        scope.cancel()
        scope = MainScope()
    }

    fun select(clingDevice: ClingDevice) {
        initIfNeed()
        scope.launch {
            curDevice.value = clingDevice
            ClingManager.getInstance().selectedDevice = clingDevice
        }
    }

    fun playNew(url: String) {
        initIfNeed()
        url.moeSnackBar()
        scope.launch {
            val callback = object : ControlCallback<Any> {
                override fun success(response: IResponse<Any>?) {
                    response?.response?.moeSnackBar()
                    response?.response?.let {
                        Log.d(TAG, it.toString())
                    }
                }

                override fun fail(response: IResponse<Any>?) {
                    response?.response?.moeSnackBar()
                    response?.response?.let {
                        Log.d(TAG, it.toString())
                    }
                }
            }
            mClingPlayControl.playNew(url, callback)
        }
    }

    fun play() {
        initIfNeed()
        scope.launch {
            val callback = object : ControlCallback<Any> {
                override fun success(response: IResponse<Any>?) {

                }

                override fun fail(response: IResponse<Any>?) {

                }
            }
            mClingPlayControl.play(callback)
        }
    }

    fun pause() {
        initIfNeed()
        scope.launch {
            val callback = object : ControlCallback<Any> {
                override fun success(response: IResponse<Any>?) {

                }

                override fun fail(response: IResponse<Any>?) {

                }
            }
            mClingPlayControl.pause(callback)
        }
    }

    fun stop() {
        initIfNeed()
        scope.launch {
            val callback = object : ControlCallback<Any> {
                override fun success(response: IResponse<Any>?) {

                }

                override fun fail(response: IResponse<Any>?) {

                }
            }
            mClingPlayControl.stop(callback)
        }
    }


    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.e(TAG, "Receive playback intent:$action")
            if (Intents.ACTION_PLAYING.equals(action)) {
                scope.launch {
                    stringRes(R.string.dlne_state_playing).moeSnackBar()
                }
            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                scope.launch {
                    stringRes(R.string.dlne_state_pause).moeSnackBar()
                }
            } else if (Intents.ACTION_STOPPED.equals(action)) {
                scope.launch {
                    stringRes(R.string.dlne_state_stop).moeSnackBar()
                }
            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                scope.launch {
                    stringRes(R.string.dlne_state_loading).moeSnackBar()
                }
            }
        }
    }

}