package com.heyanle.easybangumi4.getter

import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.source.bundle.SourceBundle
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
        return sourceController.configSource.map { SourceBundle(it) }.first()
    }

    fun flowBundle(): Flow<SourceBundle> {
        return sourceController.configSource.map { SourceBundle(it) }
    }

    fun flowState(): StateFlow<SourceController.SourceInfoState> {
        return sourceController.sourceInfo
    }

}