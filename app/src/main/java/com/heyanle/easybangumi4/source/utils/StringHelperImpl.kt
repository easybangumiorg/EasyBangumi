package com.heyanle.easybangumi4.source.utils

import android.widget.Toast
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.source_api.utils.api.StringHelper

/**
 * Created by HeYanLe on 2023/10/29 16:29.
 * https://github.com/heyanLE
 */
object StringHelperImpl: StringHelper {

    override fun moeSnackBar(string: String) {
        string.moeSnackBar()
    }

    override fun toast(string: String) {
        Toast.makeText(APP, string, Toast.LENGTH_SHORT).show()
    }
}