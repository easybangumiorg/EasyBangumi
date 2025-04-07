package com.heyanle.lib.unifile.core

import com.heyanle.lib.unifile.AndroidUniUtils
import com.heyanle.lib.unifile.UniFile

/**
 * Created by heyanlin on 2024/12/4.
 */
interface TypeUniFile: UniFile {
    fun getType(): String {
        return AndroidUniUtils.getTypeForName(getName())
    }
}