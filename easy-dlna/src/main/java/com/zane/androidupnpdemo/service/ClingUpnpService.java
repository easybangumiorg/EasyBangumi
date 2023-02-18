package com.zane.androidupnpdemo.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.zane.androidupnpdemo.EasyAndroidRouterImpl;
import com.zane.androidupnpdemo.log.AndroidLoggingHandler;

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
import org.fourthline.cling.transport.RouterException;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/28 16:11
 */

public class ClingUpnpService extends AndroidUpnpServiceImpl {

    public static volatile boolean isCreate = false;
    private LocalDevice mLocalDevice = null;


    @Override
    public void onCreate() {
        Log.d("ClingUpnpService", "onCreate");
        //super.onCreate();

        isCreate = true;
        upnpService = new UpnpServiceImpl(getConfiguration()) {


            @Override
            protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
                return new EasyAndroidRouterImpl(configuration, protocolFactory, ClingUpnpService.this);
            }

            @Override
            public synchronized void shutdown() {
                ((AndroidRouter) getRouter()).unregisterBroadcastReceiver();

                // Now we can concurrently run the Cling shutdown code, without occupying the
                // Android main UI thread. This will complete probably after the main UI thread
                // is done.
                super.shutdown(false);
                try {
                    Log.d("ClingUpnpService", "" + getRouter().isEnabled());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //LocalBinder instead of binder
        binder = new LocalBinder();
    }

    @Override
    public void onDestroy() {
        isCreate = false;
        Log.d("ClingUpnpService", "onDestroy");
        upnpService.shutdown();
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
