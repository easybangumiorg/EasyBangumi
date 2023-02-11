package com.zane.androidupnpdemo.entity;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.support.model.PositionInfo;

/**
 * 说明：获取播放进度回调结果
 * 作者：zhouzhan
 * 日期：17/7/19 12:26
 */

public class ClingPositionResponse extends BaseClingResponse<PositionInfo> implements IResponse<PositionInfo> {


    public ClingPositionResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingPositionResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingPositionResponse(ActionInvocation actionInvocation, PositionInfo info) {
        super(actionInvocation, info);
    }
}
