package com.heyanle.easybangumi4.case

import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * Created by heyanlin on 2023/10/2.
 */
class SourceStateCase(
    private val sourceController: SourceController,
) {

    // 等到下一个番剧源就绪状态
    suspend fun awaitBundle(): SourceBundle {
        return sourceController.sourceBundle.filterIsInstance<SourceBundle>().first()
    }

    fun flowBundle(): Flow<SourceBundle> {
        return sourceController.sourceBundle.filterIsInstance()
    }

    fun flowState(): StateFlow<SourceController.SourceInfoState> {
        return sourceController.sourceInfo
    }

    fun stateFlowBundle(): StateFlow<SourceBundle?> {
        return sourceController.sourceBundle
    }

}