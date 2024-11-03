package com.heyanle.easybangumi4.plugin.js.utils;


import android.util.Log;

/**
 * Created by HeYanLe on 2024/11/3 21:42.
 * https://github.com/heyanLE
 */

public class JSLogUtils {

    public static void i(String tag, String msg){
        Log.i(tag, msg);
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
