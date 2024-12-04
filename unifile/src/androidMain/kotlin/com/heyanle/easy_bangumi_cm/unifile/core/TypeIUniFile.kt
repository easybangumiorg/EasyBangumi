package com.heyanle.easy_bangumi_cm.unifile.core

import com.heyanle.easy_bangumi_cm.unifile.AndroidUniUtils
import com.heyanle.easy_bangumi_cm.unifile.IUniFile

/**
 * Created by heyanlin on 2024/12/4.
 */
interface TypeIUniFile: IUniFile {
    fun getType(): String {
        return AndroidUniUtils.getTypeForName(getName())
    }
}