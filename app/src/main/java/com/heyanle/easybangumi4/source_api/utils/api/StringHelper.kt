package com.heyanle.easybangumi4.source_api.utils.api

/**
 * Created by HeYanLe on 2023/10/19 0:04.
 * https://github.com/heyanLE
 */
interface StringHelper {

    /**
     * 展示纯纯看番样式对话框通知
     */
    fun moeDialog(text: String, title: String? = null)

    /**
     * 展示纯纯看番样式站内通知
     */
    fun moeSnackBar(string: String)

    /**
     * 弹 toast
     */
    fun toast(string: String)

}