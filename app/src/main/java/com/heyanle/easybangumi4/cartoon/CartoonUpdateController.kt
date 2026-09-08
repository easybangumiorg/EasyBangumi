package com.heyanle.easybangumi4.cartoon

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class CartoonUpdateController(
    private val cartoonRepository: CartoonRepository,
) {

    private val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    fun update(
        list: Collection<CartoonInfo>
    ) {
        scope.launch {
            innerUpdate(list, false)
        }
    }

    fun updateAll() {
        scope.launch {
            innerUpdate(cartoonRepository.flowAllStar().first(), false)
        }
    }

    private suspend fun innerUpdate(
        list: Collection<CartoonInfo>,
        isStrict: Boolean
    ) {
        _isUpdating.value = true
        try {
            list.asSequence()
                .filter { it.isDetailed && it.lastHistoryTime != 0L }
                .filter {
                    when (it.updateStrategy) {
                        Cartoon.UPDATE_STRATEGY_ALWAYS -> true
                        Cartoon.UPDATE_STRATEGY_ONLY_STRICT -> isStrict
                        else -> false
                    }
                }
                .forEach {
                    cartoonRepository.awaitCartoonInfoWithPlayLines(
                        it.toSummary(),
                        forceRefresh = true,
                    )
                }
        } finally {
            _isUpdating.value = false
        }
    }


}
