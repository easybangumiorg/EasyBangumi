package com.zane.androidupnpdemo.entity;

/**
 * 说明：控制点
 * 作者：zhouzhan
 * 日期：17/6/27 17:47
 */

public interface IControlPoint<T> {

    /**
     * @return 返回控制点
     */
    T getControlPoint();

    /**
     * 设置控制点
     *
     * @param controlPoint 控制点
     */
    void setControlPoint(T controlPoint);

    /**
     * 销毁 清空缓存
     */
    void destroy();
}
