package com.zane.androidupnpdemo.entity;

import org.fourthline.cling.controlpoint.ControlPoint;

/**
 * 说明：cling 控制点实现类
 * 作者：zhouzhan
 * 日期：17/6/28 17:03
 */

public class ClingControlPoint implements IControlPoint<ControlPoint> {

    private static ClingControlPoint INSTANCE = null;
    private ControlPoint mControlPoint;

    private ClingControlPoint() {
    }

    public static ClingControlPoint getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClingControlPoint();
        }
        return INSTANCE;
    }

    @Override
    public ControlPoint getControlPoint() {
        return mControlPoint;
    }

    @Override
    public void setControlPoint(ControlPoint controlPoint) {
        mControlPoint = controlPoint;
    }

    @Override
    public void destroy() {
        mControlPoint = null;
        INSTANCE = null;
    }
}
