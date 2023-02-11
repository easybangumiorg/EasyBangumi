package com.zane.androidupnpdemo.service.manager;


import android.content.Context;

import com.zane.androidupnpdemo.entity.IControlPoint;
import com.zane.androidupnpdemo.entity.IDevice;

import java.util.Collection;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/27 17:41
 */

public interface IDLNAManager {

    /**
     * 搜索所有的设备
     */
    void searchDevices();

    /**
     * 获取支持 Media 类型的设备
     *
     * @return 设备列表
     */
    Collection<? extends IDevice> getDmrDevices();

    /**
     * 获取控制点
     *
     * @return 控制点
     */
    IControlPoint getControlPoint();

    /**
     * 获取选中的设备
     *
     * @return 选中的设备
     */
    IDevice getSelectedDevice();

    /**
     * 取消选中的设备
     */
    void cleanSelectedDevice();

    /**
     * 设置选中的设备
     *
     * @param device 已选中设备
     */
    void setSelectedDevice(IDevice device);

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
