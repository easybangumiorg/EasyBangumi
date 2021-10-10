package com.heyanle.easybangumi.utils.oksp

/**
 * Created by HeYanLe on 2021/10/10 15:22.
 * https://github.com/heyanLE
 */
interface OKSPDecoder <T> {

    fun decoder(source: String, value: T?): T{
        return source as T
    }

}