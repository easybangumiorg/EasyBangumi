package com.zane.androidupnpdemo.control.callback;

import com.zane.androidupnpdemo.entity.IResponse;

/**
 * 说明：设备控制操作 回调
 * 作者：zhouzhan
 * 日期：17/7/4 10:56
 */

public interface ControlCallback<T> {

    void success(IResponse<T> response);

    void fail(IResponse<T> response);
}
