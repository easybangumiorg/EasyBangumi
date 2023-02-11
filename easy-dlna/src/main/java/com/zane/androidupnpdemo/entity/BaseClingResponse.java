package com.zane.androidupnpdemo.entity;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;

/**
 * 说明：Cling 服务回调基类
 * 作者：zhouzhan
 * 日期：17/7/19 16:25
 */

public class BaseClingResponse<T> implements IResponse<T> {

    protected ActionInvocation mActionInvocation;
    protected UpnpResponse operation;
    protected String defaultMsg;
    protected T info;

    /**
     * 控制操作成功 构造器
     *
     * @param actionInvocation cling action 调用
     */
    public BaseClingResponse(ActionInvocation actionInvocation) {
        mActionInvocation = actionInvocation;
    }

    /**
     * 控制操作失败 构造器
     *
     * @param actionInvocation cling action 调用
     * @param operation        执行状态
     * @param defaultMsg       错误信息
     */
    public BaseClingResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        mActionInvocation = actionInvocation;
        this.operation = operation;
        this.defaultMsg = defaultMsg;
    }

    /**
     * 接收时的回调
     *
     * @param actionInvocation cling action 调用
     * @param info             回调的对象
     */
    public BaseClingResponse(ActionInvocation actionInvocation, T info) {
        mActionInvocation = actionInvocation;
        this.info = info;
    }

    @Override
    public T getResponse() {
        return info;
    }

    @Override
    public void setResponse(T response) {
        info = response;
    }
}
