package com.heyanle.easy_bangumi_cm.common.plugin.core

import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.SourceInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource
import kotlinx.coroutines.flow.Flow

/**
 * Created by heyanlin on 2025/1/29.
 */
interface InnerSourceProvider {

    fun flowInnerSource(): Flow<List<InnerSource>>

}