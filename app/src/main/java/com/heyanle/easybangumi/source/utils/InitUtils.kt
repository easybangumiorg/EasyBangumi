package com.heyanle.easybangumi.source.utils

import com.heyanle.bangumi_source_api.api.utils.pathUtil
import com.heyanle.bangumi_source_api.api.utils.stringUtil
import com.heyanle.bangumi_source_api.api.utils.webUtil

/**
 * Created by HeYanLe on 2023/2/1 17:49.
 * https://github.com/heyanLE
 */
fun initUtils(){
    pathUtil = PathUtilsImpl()
    stringUtil = StringUtilsImpl()
    webUtil = WebUtilImpl
}