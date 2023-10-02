package com.heyanle.easybangumi4.getter

import com.heyanle.easybangumi4.source.SourceBundle
import com.heyanle.easybangumi4.source.SourceController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by heyanlin on 2023/10/2.
 */
class SourceStateGetter(
    private val sourceController: SourceController,
) {

    // 等到下一个番剧源就绪状态
    suspend fun awaitBundle(): SourceBundle {
        return sourceController.sourceState.filterIsInstance<SourceController.SourceState.Completely>().first().sourceBundle
    }

    fun flowBundle(): Flow<SourceBundle> {
        return sourceController.sourceState.filterIsInstance<SourceController.SourceState.Completely>().map { it.sourceBundle }
    }

    fun flowState(): StateFlow<SourceController.SourceState> {
        return sourceController.sourceState
    }

}