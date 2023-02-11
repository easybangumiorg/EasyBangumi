package com.zane.androidupnpdemo.entity;

/**
 * 说明：设备控制 返回结果
 * 作者：zhouzhan
 * 日期：17/7/4 10:50
 */

public interface IResponse<T> {

    T getResponse();

    void setResponse(T response);
}
