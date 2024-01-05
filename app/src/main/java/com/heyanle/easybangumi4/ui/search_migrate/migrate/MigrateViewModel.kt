package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.case.CartoonInfoCase
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.ViewModelOwnerMap
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.utils.systemVolume

/**
 * Created by heyanle on 2023/12/23.
 * https://github.com/heyanLE
 */
class MigrateViewModel(
    private val summaries: List<CartoonSummary>,
    private val sources: List<String>,
) : ViewModel() {


    private val ownerMap = ViewModelOwnerMap<CartoonInfo>()


    private val cartoonInfoDao: CartoonInfoDao by Injekt.injectLazy()
    private val cartoonInfoCase: CartoonInfoCase by Injekt.injectLazy()
    private val sourceCase: SourceStateCase by Injekt.injectLazy()

    private val migrateDispatcher = CoroutineProvider.SINGLE


    data class MigrateState(
        val isLoading: Boolean = true,
        val isMigrating: Boolean = false,
        val infoList: List<CartoonInfo> = emptyList(),
        val selection: Set<CartoonInfo> = emptySet(),
    )

    private val _infoListFlow = MutableStateFlow<MigrateState>(MigrateState())
    val infoListFlow = _infoListFlow.asStateFlow()

    private var lastSelectInfo: CartoonInfo? = null

    init {
        viewModelScope.launch {
            val infoList = summaries.map {
                cartoonInfoCase.awaitCartoonInfoWithPlayLines(it.id, it.source, it.url)
            }.filterIsInstance<DataResult.Ok<CartoonInfo>>()
                .map {
                    it.data
                }
            _infoListFlow.update {
                it.copy(
                    false,
                    infoList = infoList
                )
            }
        }
    }

    fun getOwner(cartoonInfo: CartoonInfo) = ownerMap.getViewModelStoreOwner(cartoonInfo)
    fun getItemViewModelFactory(cartoonInfo: CartoonInfo) =
        MigrateItemViewModelFactory(cartoonInfo, sources)

    override fun onCleared() {
        super.onCleared()
        ownerMap.clear()
    }


    fun selectChange(cartoonInfo: CartoonInfo) {
        _infoListFlow.update {
            it.copy(
                selection = it.selection.toMutableSet().apply {
                    if (contains(cartoonInfo)) {
                        remove(cartoonInfo)
                    } else {
                        add(cartoonInfo)
                    }
                }
            )
        }
        lastSelectInfo = if (_infoListFlow.value.selection.isEmpty()) {
            null
        } else {
            cartoonInfo
        }

    }

    fun selectLongPress(cartoonInfo: CartoonInfo) {
        if (lastSelectInfo == null) {
            selectChange(cartoonInfo)
            return
        }
        val currInfo = _infoListFlow.value.infoList
        val lastIndex = currInfo.indexOf(lastSelectInfo)
        if (lastIndex == -1) {
            selectChange(cartoonInfo)
            return
        }
        val nowIndex = currInfo.indexOf(cartoonInfo)
        if (nowIndex == -1) {
            selectChange(cartoonInfo)
            return
        }
        val min = lastIndex.coerceAtMost(nowIndex)
        val max = lastIndex.coerceAtLeast(nowIndex)

        _infoListFlow.update {
            it.copy(
                selection = it.selection.toMutableSet().apply {
                    for (i in min..max) {
                        val car = currInfo.getOrNull(i) ?: continue
                        if (contains(car)) {
                            remove(car)
                        } else {
                            add(car)
                        }
                    }

                }
            )
        }
        lastSelectInfo = if (_infoListFlow.value.selection.isEmpty()) {
            null
        } else {
            cartoonInfo
        }
    }

    fun selectExit() {
        _infoListFlow.update {
            it.copy(selection = setOf())
        }
        lastSelectInfo = null
    }

    fun selectAll() {
        _infoListFlow.update {
            it.copy(
                selection = it.infoList.toSet()
            )
        }
    }

    fun selectInvert() {
        _infoListFlow.update {
            val currInfo = _infoListFlow.value.infoList
            it.copy(
                selection = it.selection.toMutableSet().apply {
                    for (car in currInfo) {
                        if (contains(car)) {
                            remove(car)
                        } else {
                            add(car)
                        }
                    }
                }
            )
        }
    }

    fun remove(cartoonInfo: CartoonInfo) {
        _infoListFlow.update {
            it.copy(
                infoList = it.infoList.filter { it != cartoonInfo }
            )
        }
    }

    fun migrate(
        migrate: List<Pair<CartoonInfo, MigrateItemViewModel.MigrateItemState>>,
        onCompletely: () -> Unit = {}
    ) {

        viewModelScope.launch(migrateDispatcher) {
            _infoListFlow.update {
                it.copy(
                    isMigrating = true
                )
            }

            val bundle = sourceCase.awaitBundle()
            val actions = arrayListOf<Pair<CartoonInfo, CartoonInfo>>()
            for (pair in migrate) {
                val car = pair.second.cartoon ?: continue
                val sourceName = bundle.source(car.source)?.label ?: ""
                val episodeList = pair.second.playLineWrapper?.sortedEpisodeList
                val cartoonInfo = CartoonInfo.fromCartoon(
                    car,
                    sourceName,
                    pair.second.playLineList
                ).copy(
                    tags = pair.first.tags,
                    upTime = pair.first.upTime,

                    starTime = if (pair.first.starTime == 0L) System.currentTimeMillis() else pair.first.starTime,
                    lastHistoryTime = pair.first.lastHistoryTime,
                    lastPlayLineEpisodeString = episodeList?.toJson() ?: "",
                    lastLineId = pair.second.playLine?.id ?: "",
                    lastLinesIndex = pair.second.playLineList.indexOf(pair.second.playLine) ?: -1,
                    lastLineLabel = pair.second.playLine?.label ?: "",

                    lastEpisodeLabel = pair.second.playLine?.label ?: "",
                    lastEpisodeId = pair.second.episode?.id ?: "",
                    lastEpisodeIndex = episodeList?.indexOf(pair.second.episode) ?: -1,
                    lastEpisodeOrder = pair.second.episode?.order ?: -1,

                    lastProcessTime = 0,
                )
                actions.add(pair.first to cartoonInfo)
            }

            actions.map {
                async {
                    cartoonInfoDao.modify(it.second)
                    cartoonInfoDao.deleteStar(it.first)
                }
            }.forEach {
                it.await()
            }

            _infoListFlow.update {
                it.copy(
                    isMigrating = false
                )
            }

            viewModelScope.launch {
                onCompletely()
            }
        }
    }

}

class MigrateViewModelFactory(
    private val summaries: List<CartoonSummary>,
    private val sources: List<String>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MigrateViewModel::class.java))
            return MigrateViewModel(summaries, sources) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}