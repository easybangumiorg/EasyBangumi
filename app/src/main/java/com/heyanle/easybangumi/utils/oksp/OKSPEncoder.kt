package com.heyanle.easybangumi.utils.oksp

/**
 * Created by HeYanLe on 2021/10/10 15:23.
 * https://github.com/heyanLE
 */
interface OKSPEncoder <T> {

    fun encode(value:T): String{
        return value.toString()
    }

}