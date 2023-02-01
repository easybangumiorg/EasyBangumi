package com.heyanle.easybangumi.source.utils

import com.heyanle.bangumi_source_api.api.utils.StringUtils
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.utils.toast

/**
 * Created by HeYanLe on 2023/2/1 17:31.
 * https://github.com/heyanLE
 */
class StringUtilsImpl: StringUtils {

    override fun imeSnackBar(content: String) {
        content.moeSnackBar()
    }

    override fun toast(content: String) {
        content.toast()
    }
}