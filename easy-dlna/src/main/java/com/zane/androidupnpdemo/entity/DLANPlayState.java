package com.zane.androidupnpdemo.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 说明：连接设备状态
 * 作者：zhouzhan
 * 日期：17/7/10 17:21
 */

public class DLANPlayState {
    /**
     * 播放状态
     */
    public static final int PLAY = 1;
    /**
     * 暂停状态
     */
    public static final int PAUSE = 2;
    /**
     * 停止状态
     */
    public static final int STOP = 3;
    /**
     * 转菊花状态
     */
    public static final int BUFFER = 4;
    /**
     * 投放失败
     */
    public static final int ERROR = 5;

    /**
     * 设备状态
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface DLANPlayStates {
    }


    // 以下不算设备状态, 只是常量
    /**
     * 主动轮询获取播放进度(在远程设备不支持播放进度回传时使用)
     */
    public static final int GET_POSITION_POLING = 6;
    /**
     * 远程设备播放进度回传
     */
    public static final int POSITION_CALLBACK = 7;
    /**
     * 投屏端播放完成
     */
    public static final int PLAY_COMPLETE = 8;
}
