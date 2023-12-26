package com.heyanle.easybangumi4.source_old.utils

import android.widget.Toast
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.lib_anim.utils.StringHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/3 23:19.
 * https://github.com/heyanLE
 */
class StringHelperImpl : StringHelper {

    override fun moeSnackBar(string: String) {
        GlobalScope.launch(Dispatchers.Main) {
            string.moeSnackBar()
        }

    }

    override fun toast(string: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(APP, string, Toast.LENGTH_SHORT).show()
        }

    }
}