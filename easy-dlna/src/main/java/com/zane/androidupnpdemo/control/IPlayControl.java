package com.zane.androidupnpdemo.control;

import android.os.Handler;

import com.zane.androidupnpdemo.control.callback.ControlCallback;
import com.zane.androidupnpdemo.control.callback.ControlReceiveCallback;

import org.jetbrains.annotations.Nullable;

/**
 * 说明：对视频的控制操作定义
 * 作者：zhouzhan
 * 日期：17/6/27 17:13
 */
public interface IPlayControl {

    /**
     * 播放一个新片源
     *
     * @param url 片源地址
     */
    void playNew(String url, @Nullable ControlCallback callback);

    /**
     * 播放
     */
    void play(@Nullable ControlCallback callback);

    /**
     * 暂停
     */
    void pause(@Nullable ControlCallback callback);

    /**
     * 停止
     */
    void stop(@Nullable ControlCallback callback);

    /**
     * 视频 seek
     *
     * @param pos seek到的位置(单位:毫秒)
     */
    void seek(int pos, @Nullable ControlCallback callback);

    /**
     * 设置音量
     *
     * @param pos 音量值，最大为 100，最小为 0
     */
    void setVolume(int pos, @Nullable ControlCallback callback);

    /**
     * 设置静音
     *
     * @param desiredMute 是否静音
     */
    void setMute(boolean desiredMute, @Nullable ControlCallback callback);

    /**
     * 获取tv进度
     */
    void getPositionInfo(@Nullable ControlReceiveCallback callback);

    /**
     * 获取音量
     */
    void getVolume(@Nullable ControlReceiveCallback callback);
}
