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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/19 16:43.
 * https://github.com/heyanLE
 */
object UpdateMaster {

    private val scope = MainScope()
    private var job: Job? = null

    var isLoading = MutableStateFlow(false)
    var loadingError = MutableStateFlow<String?>(null)
    var updateCount = mutableStateOf(0)

    var updatingCartoonName= MutableStateFlow<String>("")

    fun tryUpdate(
        list: List<CartoonStar>
    ): Boolean {
        return update(list.asFlow())
    }

    fun tryUpdate(
        isStrict: Boolean
    ): Boolean {
        val flow = DB.cartoonStar.getAll().asFlow()
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
        return update(flow)
    }

    private fun update(flow: Flow<CartoonStar>): Boolean {
        return if (isLoading.compareAndSet(expect = false, update = true)) {
            job?.cancel("New Update Task")
            job = scope.launch(Dispatchers.IO) {
                flow.map { star ->
                    star to async {
                        star.toCartoon()?.let { cartoon ->
                            (SourceMaster.animSourceFlow.value.update(cartoon.source)
                                ?.update(
                                    cartoon,
                                    star.getPlayLine()
                                ) as? SourceResult.Complete<Cartoon>)?.data
                        }
                    }
                }.map {
                    updatingCartoonName.update { _ ->
                        it.first.title
                    }
                    it.first to it.second.await()
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
                        DB.cartoonStar.modify(it)
                        loadingError.update {
                            null
                        }
                    }
                isLoading.compareAndSet(expect = true, update = false)
            }
            true
        } else {
            false
        }
    }

}