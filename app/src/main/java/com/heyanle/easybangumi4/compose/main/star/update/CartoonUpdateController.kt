package com.heyanle.easybangumi4.compose.main.star.update

import androidx.compose.runtime.mutableStateOf
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.easybangumi4.base.db.dao.CartoonStarDao
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/19 16:43.
 * https://github.com/heyanLE
 */
class CartoonUpdateController(
    private val cartoonStarDao: CartoonStarDao,
    private val sourceLibraryController: SourceLibraryController,
) {

    private val scope = MainScope()
    private var job: Job? = null

    private var lastUpdateTimeOkkv by okkv("LastUpdateTime", -1L, ignoreException = false)
    var lastUpdateTime = MutableStateFlow(lastUpdateTimeOkkv)

    var isUpdate = MutableStateFlow(false)
    var loadingError = MutableStateFlow<String?>(null)
    var updateCount = mutableStateOf(0)

    fun tryUpdate(
        list: List<CartoonStar>
    ): Boolean {
        return update(list.asFlow(), false)
    }

    suspend fun tryUpdate(
        isStrict: Boolean
    ): Boolean {
        val flow = withContext(Dispatchers.IO) {
            cartoonStarDao.getAll().asFlow()
                .filter {
                    it.isInitializer && !it.isUpdate
                }
                .filter {
                    when (it.updateStrategy) {
                        Cartoon.UPDATE_STRATEGY_ALWAYS -> true
                        Cartoon.UPDATE_STRATEGY_ONLY_STRICT -> isStrict
                        else -> false
                    }
                }
        }
        return update(flow, true)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun update(flow: Flow<CartoonStar>, isAll: Boolean): Boolean {
        return if (isUpdate.compareAndSet(expect = false, update = true)) {
            job?.cancel("New Update Task")
            job = scope.launch(Dispatchers.IO) {
                flow.flatMapMerge(3) { star ->
                    kotlinx.coroutines.flow.flow {
                        kotlin.runCatching {
                            emit(star to star.toCartoon()?.let { cartoon ->
                                cartoon.source.loge("UpdateMaster ")
                                val res =
                                    (sourceLibraryController.sourceBundleFlow.value.update(cartoon.source)
                                        ?.update(
                                            cartoon,
                                            star.getPlayLine()
                                        ))
                                res.loge("UpdateMaster")
                                (res as? SourceResult.Complete<Cartoon>)?.data
                            })
                        }.getOrElse {
                            it.printStackTrace()
                        }
                    }
                }.filter {
                    it.second != null
                }.filterIsInstance<Pair<CartoonStar, Cartoon>>()
                    .map {
                        CartoonStar.fromCartoon(it.second, it.first.getPlayLine())
                    }
                    .catch { throwable ->
                        throwable.printStackTrace()
                        loadingError.update {
                            throwable.message ?: throwable.toString()
                        }
                    }
                    .toList().let {
                        val now = System.currentTimeMillis()
                        cartoonStarDao.modify(it, now)
                        loadingError.update {
                            null
                        }
                        if (isAll) {
                            lastUpdateTimeOkkv = now
                            lastUpdateTime.update {
                                now
                            }
                        }
                        isUpdate.update {
                            false
                        }
                    }


            }
            true
        } else {
            false
        }
    }

}