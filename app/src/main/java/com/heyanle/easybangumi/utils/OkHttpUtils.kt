package com.heyanle.easybangumi.utils

import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by HeYanLe on 2021/10/14 19:44.
 * https://github.com/heyanLE
 */
object OkHttpUtils {

    private val okhttpClient =  OkHttpClient()

    fun get(url: String): String{
        return okhttpClient.newCall(Request.Builder().url(url).header("User-Agent",
            EasyApplication.INSTANCE.getString(R.string.UA)).get().build()).execute().body!!.string()
    }

}