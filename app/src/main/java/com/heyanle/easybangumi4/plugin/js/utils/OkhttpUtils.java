package com.heyanle.easybangumi4.plugin.js.utils;

import com.google.gson.JsonObject;
import com.heyanle.easybangumi4.source_api.utils.core.network.RequestKt;

import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by heyanle on 2025/8/3
 * https://github.com/heyanLE
 */
public class OkhttpUtils {

    public static Request get(String url){
        return OkhttpRequestKtWrapper.INSTANCE.get(url);
    }

    public static Request get(String url, Map<String, String> header){
        return OkhttpRequestKtWrapper.INSTANCE.get(url, header);
    }

    public static Request post(String url){
        return OkhttpRequestKtWrapper.INSTANCE.post(url);
    }

    public static Request postFromBody(String url, Map<String, Object> fromBody){
        return OkhttpRequestKtWrapper.INSTANCE.postFormBody(url, fromBody);
    }




}
