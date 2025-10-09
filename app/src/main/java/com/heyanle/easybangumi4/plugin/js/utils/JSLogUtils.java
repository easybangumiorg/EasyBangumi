package com.heyanle.easybangumi4.plugin.js.utils;


import static com.tencent.bugly.proguard.at.i;

import android.util.Log;

/**
 * Created by HeYanLe on 2024/11/3 21:42.
 * https://github.com/heyanLE
 */

public class JSLogUtils {

    public static void i(String tag, String msg){
        if (msg.startsWith("PlayComponent_getPlayInfo")) {
            return;
        }
        if (msg.length() > 4000) {
            int chunkCount = msg.length() / 4000;     // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= msg.length()) {
                    Log.i(tag, msg.substring(4000 * i));
                } else {
                    Log.i(tag, msg.substring(4000 * i, max));
                }
            }
        } else {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg){
        Log.e(tag, msg);
    }

    public static void d(String tag, String msg){
        Log.d(tag, msg);
    }

    public static void w(String tag, String msg){
        Log.w(tag, msg);
    }

    public static void v(String tag, String msg){
        Log.v(tag, msg);
    }

    public static void wtf(String tag, String msg){
        Log.wtf(tag, msg);
    }

}
