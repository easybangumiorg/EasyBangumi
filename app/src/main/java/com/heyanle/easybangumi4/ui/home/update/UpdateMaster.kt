package com.heyanle.easybangumi4.ui.home.update

import androidx.compose.runtime.mutableStateOf
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.easybangumi4.DB
import com.heyanle.easybangumi4.db.entity.CartoonStar
import com.heyanle.easybangumi4.source.SourceMaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/19 16:43.
 * https://github.com/heyanLE
 */
object UpdateMaster {

    private val scope = MainScope()
    private var job: Job? = null

    var isLoading = MutableStateFlow(false)
    var updateCount = mutableStateOf(0)

    fun tryUpdate(
        strict: Boolean,
    ): Boolean {
        if (isLoading.compareAndSet(expect = false, update = true)) {
            job?.cancel()
            val bundle = SourceMaster.animSourceFlow.value
            scope.launch(Dispatchers.Main) {
                updateCount.value = 0
            }
            job = scope.launch(Dispatchers.IO) {
                val list = DB.cartoonStar.getAllNormal()
                    .asFlow()
                    .filter {
                        // 只有初始化了并且没更新的 才需要检查更新
                        it.isInitializer && !it.isUpdate
                    }
                    .filter {
                        // 根据更新策略来过滤
                        when (it.updateStrategy) {
                            Cartoon.UPDATE_STRATEGY_ALWAYS -> true
                            Cartoon.UPDATE_STRATEGY_ONLY_STRICT -> strict
                            else -> false
                        }
                    }.transform {
                        val updateCartoon = bundle.update(it.source)
                        val cartoon = it.toCartoon()
                        if (updateCartoon != null && cartoon != null) {
                            emit(Triple(it, cartoon, updateCartoon))
                        }
                    }
                    .transform {
                        emit(it.first to async {
                            it.third.update(it.second, it.first.getPlayLine())
                        })

                    }.transform {
                        emit(it.first to it.second.await())
                    }.transform {
                        val cartoon = (it.second as? SourceResult.Complete)?.data
                            ?: return@transform
                        emit(CartoonStar.fromCartoon(cartoon, it.first.getPlayLine()))
                    }.toList()
                DB.cartoonStar.modify(list)
                withContext(Dispatchers.Main) {
                    updateCount.value = list.count {
                        it.isUpdate
                    }
                }
            }
            return true
        } else {
            return false
        }
    }


}