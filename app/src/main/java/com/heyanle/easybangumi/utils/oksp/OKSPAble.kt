package com.heyanle.easybangumi.utils.oksp

/**
 * Created by HeYanLe on 2021/10/10 15:41.
 * https://github.com/heyanLE
 */
interface OKSPAble<T> {

    fun getEncoder(): OKSPEncoder<T>

    fun getDecoder(): OKSPDecoder<T>

}