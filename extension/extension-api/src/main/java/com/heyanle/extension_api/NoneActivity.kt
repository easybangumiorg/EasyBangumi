package com.heyanle.extension_api

import android.app.Activity
import android.os.Bundle

/**
 * Created by HeYanLe on 2023/2/22 23:43.
 * https://github.com/heyanLE
 */
class NoneActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw IllegalAccessException()
    }
}