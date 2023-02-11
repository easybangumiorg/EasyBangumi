package com.zane.androidupnpdemo.service.manager;

import android.content.Context;

import com.zane.androidupnpdemo.entity.IDevice;


/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/7/21 16:34
 */

public interface IDeviceManager {

    /**
     * 获取选中设备
     */
    IDevice getSelectedDevice();

    /**
     * 设置选中设备
     */
    void setSelectedDevice(IDevice selectedDevice);

    /**
     * 取消选中设备
     */
    void cleanSelectedDevice();

    /**
     * 监听投屏端 AVTransport 回调
     *
     * @param context 用于接收到消息发广播
     */
    void registerAVTransport(Context context);

    /**
     * 监听投屏端 RenderingControl 回调
     *
     * @param context 用于接收到消息发广播
     */
    void registerRenderingControl(Context context);

    /**
     * 销毁
     */
    void destroy();
}
