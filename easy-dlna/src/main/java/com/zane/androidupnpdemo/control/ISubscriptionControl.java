package com.zane.androidupnpdemo.control;

import android.content.Context;

import com.zane.androidupnpdemo.entity.IDevice;


/**
 * 说明：tv端回调
 * 作者：zhouzhan
 * 日期：17/7/21 16:38
 */

public interface ISubscriptionControl<T> {

    /**
     * 监听投屏端 AVTransport 回调
     */
    void registerAVTransport(IDevice<T> device, Context context);

    /**
     * 监听投屏端 RenderingControl 回调
     */
    void registerRenderingControl(IDevice<T> device, Context context);

    /**
     * 销毁: 释放资源
     */
    void destroy();
}
