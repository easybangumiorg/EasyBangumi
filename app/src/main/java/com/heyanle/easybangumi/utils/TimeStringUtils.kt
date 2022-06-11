package com.heyanle.easybangumi.utils

import java.lang.StringBuilder

/**
 * Created by HeYanLe on 2021/10/8 22:16.
 * https://github.com/heyanLE
 */
object TimeStringUtils {

    fun toTImeStringMill(time: Long): String{
        val sb = StringBuilder()
        var t = time
        // 小时
        if(t >= 3600000){
            val tt = t / 3600000
            sb.append(if(tt >= 10) tt else "0$tt")
            sb.append(":")
        }
        t %= 3600000
        // 分钟
        if(t >= 60000){
            val tt = t / 60000
            sb.append(if(tt >= 10) tt else "0$tt")
            sb.append(":")
        }else {
            sb.append("00:")
        }

        // 秒
        t %= 60000
        if(t >= 1000){
            val tt = t / 1000
            sb.append(if(tt >= 10) tt else "0$tt")
        }else if(sb.isNotEmpty()){
            sb.append("00")
        }
        return sb.toString()
    }

}