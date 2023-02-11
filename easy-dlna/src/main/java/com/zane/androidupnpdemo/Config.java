package com.zane.androidupnpdemo;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/7/6 11:32
 */

public class Config {

    // mp4 格式
    //http://mp4.res.hunantv.com/video/1155/79c71f27a58042b23776691d206d23bf.mp4
    // ts 格式
//    public static String TEST_URL = "http://ottvideows.hifuntv.com/b36ea6f167c7b5785f3aa46c47b6d983/595f51c1/internettv/c1/2017/03/29/41E0B7C03C15AD472DB008A5FF4566EB.ts?uuid=0c18530ecda4454db49665b178396ff7";
    // m3u8 格式
    public static String TEST_URL = "https://https-play-g3proxy.lecloud.com/vod/v2/MjYwLzM5LzEwNy9iY2xvdWQvMTAwMDAxL3Zlcl8wMF8yMi0xMTAwMzg4MTg5LWF2Yy00MTg4NTgtYWFjLTQ4MDAwLTU1MzYwLTMzMTk4MDYtMjUzY2MwODk0ODgyZDE3OGRiM2VjMjc3OTliMzc5ODYtMTQ5NDM4MzI2NTkzOC5tcDQ=?b=479&mmsid=234064503&tm=1496913122&pip=cbfbd2fac71c5662777858b9b5e8b611&key=d15f73e306619c49145da43a477197e2&platid=2&splatid=209&payff=0&cuid=100001&vtype=13&dur=55&p1=3&p2=31&p3=311&cf=h5-ios&p=101&playid=0&tss=ios&tag=mobile&sign=bcloud_100001&termid=2&pay=0&ostype=ios&hwtype=un&ajax=1";

    /*** 因为后台给的地址是固定的，如果不测试投屏，请设置为 false*/
    public static final boolean DLAN_DEBUG = true;
    /*** 轮询获取播放位置时间间隔(单位毫秒)*/
    public static final long REQUEST_GET_INFO_INTERVAL = 2000;
    /**
     * 投屏设备支持进度回传
     */
    private boolean hasRelTimePosCallback;
    private static Config mInstance;

    public static Config getInstance() {
        if (null == mInstance) {
            mInstance = new Config();
        }
        return mInstance;
    }

    public boolean getHasRelTimePosCallback() {
        return hasRelTimePosCallback;
    }

    public void setHasRelTimePosCallback(boolean hasRelTimePosCallback) {
        this.hasRelTimePosCallback = hasRelTimePosCallback;
    }

}
