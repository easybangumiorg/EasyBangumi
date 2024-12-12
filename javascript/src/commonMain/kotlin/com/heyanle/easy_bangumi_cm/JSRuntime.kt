package com.heyanle.easy_bangumi_cm

import com.whl.quickjs.wrapper.QuickJSContext
import java.util.concurrent.Executors

/**
 * Created by heyanlin on 2024/12/11.
 */
class JSRuntime {

    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        val JSContextLocal = ThreadLocal<QuickJSContext>()

    }

}