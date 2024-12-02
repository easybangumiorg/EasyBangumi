package com.heyanle.easy_bangumi_cm

import java.lang.management.ManagementFactory


/**
 * Created by HeYanLe on 2024/12/3 0:36.
 * https://github.com/heyanLE
 */

fun main(){
    System.getProperty("compose.application.resources.dir")?.let {
        println(it)
    }
}