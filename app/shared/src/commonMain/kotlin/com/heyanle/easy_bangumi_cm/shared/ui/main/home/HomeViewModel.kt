package com.heyanle.easy_bangumi_cm.shared.ui.main.home

import androidx.lifecycle.ViewModel
import com.heyanle.easy_bangumi_cm.common.plugin.core.source.SourceController
import com.heyanle.lib.inject.core.Inject


/**
 * Created by HeYanLe on 2025/1/5 23:27.
 * https://github.com/heyanLE
 */

class HomeViewModel: ViewModel() {

    private val sourceController: SourceController by Inject.injectLazy()



}