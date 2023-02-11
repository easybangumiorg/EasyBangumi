package com.zane.androidupnpdemo.service;

import android.content.Intent;
import android.os.IBinder;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidRouter;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/28 16:11
 */

public class ClingUpnpService extends AndroidUpnpServiceImpl {
    private LocalDevice mLocalDevice = null;

    @Override
    public void onCreate() {
        super.onCreate();
        upnpService = new UpnpServiceImpl(getConfiguration()) {


            @Override
            protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
                return new AndroidRouter(configuration, protocolFactory, ClingUpnpService.this);
            }

            @Override
            public synchronized void shutdown() {
                // First have to remove the receiver, so Android won't complain about it leaking
                // when the main UI thread exits.
                ((AndroidRouter) getRouter()).unregisterBroadcastReceiver();

                // Now we can concurrently run the Cling shutdown code, without occupying the
                // Android main UI thread. This will complete probably after the main UI thread
                // is done.
                super.shutdown(true);
            }
        };

        //LocalBinder instead of binder
        binder = new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public LocalDevice getLocalDevice() {
        return mLocalDevice;
    }

    public UpnpServiceConfiguration getConfiguration() {
        return new AndroidUpnpServiceConfiguration() {

            @Override
            public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
                return new UDA10ServiceDescriptorBinderImpl();
            }
        };
    }

    public Registry getRegistry() {
        return upnpService.getRegistry();
    }

    public ControlPoint getControlPoint() {
        return upnpService.getControlPoint();
    }

    public class LocalBinder extends Binder {
        public ClingUpnpService getService() {
            return ClingUpnpService.this;
        }
    }
}
