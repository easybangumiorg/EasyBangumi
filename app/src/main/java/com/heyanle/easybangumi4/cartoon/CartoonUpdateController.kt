package com.heyanle.easybangumi4.cartoon

import com.heyanle.easybangumi4.SourceResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class CartoonUpdateController(
    private val cartoonInfoDao: CartoonInfoDao,
    private val sourceStateCase: SourceStateCase,
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
            innerUpdate(cartoonInfoDao.flowAllStar().first(), false)
        }
    }

    private suspend fun innerUpdate(
        list: Collection<CartoonInfo>,
        isStrict: Boolean
    ) {
        _isUpdating.update { true }
        val bundle = sourceStateCase.awaitBundle()
        list.asSequence()
            .filter {
                it.isDetailed && it.lastHistoryTime != 0L
            }
            .filter {
                when (it.updateStrategy) {
                    Cartoon.UPDATE_STRATEGY_ALWAYS -> true
                    Cartoon.UPDATE_STRATEGY_ONLY_STRICT -> isStrict
                    else -> false
                }
            }.toList()
            .map {
                val detailed = bundle.detailed(it.source)
                if( detailed == null){
                    null
                }else{
                    val newCartoon = detailed.getAll(it.toSummary())
                    if(newCartoon is SourceResult.Complete){
                        it.copyFromCartoon(newCartoon.data.first, detailed.source.label, newCartoon.data.second)
                    }else{
                        null
                    }
                }
            }.filterIsInstance<CartoonInfo>()
            .forEach {
                cartoonInfoDao.modify(it)
            }
        _isUpdating.update {
            false
        }

    }


}