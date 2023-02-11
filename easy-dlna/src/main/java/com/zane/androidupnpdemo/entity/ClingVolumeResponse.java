package com.zane.androidupnpdemo.entity;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/7/19 16:22
 */

public class ClingVolumeResponse extends BaseClingResponse<Integer> {


    public ClingVolumeResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingVolumeResponse(ActionInvocation actionInvocation, Integer info) {
        super(actionInvocation, info);
    }
}
