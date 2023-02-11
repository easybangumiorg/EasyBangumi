package com.zane.androidupnpdemo.util;


import com.zane.androidupnpdemo.entity.IControlPoint;
import com.zane.androidupnpdemo.entity.IDevice;
import com.zane.androidupnpdemo.service.manager.ClingManager;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceType;
import org.jetbrains.annotations.Nullable;

/**
 * 说明：Cling 库使用工具类
 * 作者：zhouzhan
 * 日期：17/7/4 10:27
 */

public class ClingUtils {


    /**
     * 通过 ServiceType 获取已选择设备的服务
     *
     * @param serviceType 服务类型
     * @return 服务
     */
    @Nullable
    public static Service findServiceFromSelectedDevice(ServiceType serviceType) {
        IDevice selectedDevice = ClingManager.getInstance().getSelectedDevice();
        if (Utils.isNull(selectedDevice)) {
            return null;
        }

        Device device = (Device) selectedDevice.getDevice();
        return device.findService(serviceType);
    }

    /**
     * 获取 device 的 avt 服务
     *
     * @param device 设备
     * @return 服务
     */
    @Nullable
    public static Service findAVTServiceByDevice(Device device) {
        return device.findService(ClingManager.AV_TRANSPORT_SERVICE);
    }

    /**
     * 获取控制点
     *
     * @return 控制点
     */
    @Nullable
    public static ControlPoint getControlPoint() {
        IControlPoint controlPoint = ClingManager.getInstance().getControlPoint();
        if (Utils.isNull(controlPoint)) {
            return null;
        }

        return (ControlPoint) controlPoint.getControlPoint();
    }
}
