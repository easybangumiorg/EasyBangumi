package com.zane.androidupnpdemo

import android.content.Context
import android.util.Log
import com.zane.androidupnpdemo.service.ClingUpnpService
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.android.AndroidRouter
import org.fourthline.cling.model.message.IncomingDatagramMessage
import org.fourthline.cling.model.message.OutgoingDatagramMessage
import org.fourthline.cling.model.message.StreamRequestMessage
import org.fourthline.cling.model.message.StreamResponseMessage
import org.fourthline.cling.protocol.ProtocolFactory
import org.fourthline.cling.transport.spi.UpnpStream
import java.net.InetAddress

/**
 * Created by HeYanLe on 2023/2/12 19:36.
 * https://github.com/heyanLE
 */
class EasyAndroidRouterImpl(
    configuration: UpnpServiceConfiguration?,
    protocolFactory: ProtocolFactory?,
    context: Context?
) : AndroidRouter(configuration, protocolFactory, context) {

    override fun received(msg: IncomingDatagramMessage<*>?) {
        Log.d("EasyAndroidRouterImpl", "received1 ${ClingUpnpService.isCreate} ${isEnabled}")
        super.received(msg)
    }

    override fun received(stream: UpnpStream?) {
        Log.d("EasyAndroidRouterImpl", "received1 ${ClingUpnpService.isCreate} ${isEnabled}")
        super.received(stream)
    }
    override fun send(msg: OutgoingDatagramMessage<*>?) {
        Log.d("EasyAndroidRouterImpl", "send1 ${ClingUpnpService.isCreate} ${isEnabled}")
        if(!ClingUpnpService.isCreate){
            for (datagramIO in datagramIOs.values) {
                kotlin.runCatching {
                    datagramIO.stop()
                }.onFailure {
                    it.printStackTrace()
                }

            }
        }
        super.send(msg)
    }


    override fun send(msg: StreamRequestMessage?): StreamResponseMessage? {
        Log.d("EasyAndroidRouterImpl", "send2 ${ClingUpnpService.isCreate} ${isEnabled}")
        if(!ClingUpnpService.isCreate){
            kotlin.runCatching {
                streamClient.stop()
            }.onFailure {
                it.printStackTrace()
            }
            return null
        }
        return super.send(msg)
    }

    override fun startAddressBasedTransports(addresses: MutableIterator<InetAddress>?) {
        super.startAddressBasedTransports(addresses)
    }

    override fun enable(): Boolean {
        Log.i("EasyAndroidRouterImpl", Log.getStackTraceString(Throwable()))
        return super.enable()
    }
}