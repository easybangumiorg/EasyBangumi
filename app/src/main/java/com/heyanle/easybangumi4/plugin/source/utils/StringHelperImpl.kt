package com.heyanle.easybangumi4.plugin.source.utils

import android.widget.Toast
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper
import com.heyanle.easybangumi4.ui.common.moeDialog
import com.heyanle.easybangumi4.ui.common.moeSnackBar

/**
 * Created by HeYanLe on 2023/10/29 16:29.
 * https://github.com/heyanLE
 */
object StringHelperImpl: StringHelper {

    override fun moeDialog(text: String, title: String?) {
        text.moeDialog(title)
    }

    override fun moeSnackBar(string: String) {
        string.moeSnackBar()
    }

    override fun toast(string: String) {
        Toast.makeText(APP, string, Toast.LENGTH_SHORT).show()
    }
}