package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.StarMigrationResult
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.min

/**
 * Created by heyanle on 2023/12/23.
 * https://github.com/heyanLE
 */
class MigrateItemViewModel(
    private val cartoonInfo: CartoonInfo,
    private val defSources: List<String>,
) : ViewModel() {


    data class MigrateItemState(
        val isMigrated: Boolean = false,
        val isMigrating: Boolean = false,
        val isLoadingCover: Boolean = true,
        val isLoadingPlay: Boolean = true,
        val cartoonInfo: CartoonInfo,

        // 目标的番剧数据
        val cartoonCover: CartoonCover? = null,
        val cartoon: Cartoon? = null,
        val playLineList: List<PlayLine> = emptyList(),

        // 待同步的播放数据
        val sortKey: String = PlayLineWrapper.SORT_DEFAULT_KEY,
        val playLine: PlayLine? = null,
        val episode: Episode? = null,
    ){
        val playLineWrapper: PlayLineWrapper? by lazy {
            PlayLineWrapper.fromKey(playLine ?: return@lazy null, sortKey, false)
        }
    }

    private val _flow = MutableStateFlow<MigrateItemState>(MigrateItemState(cartoonInfo = cartoonInfo))
    val flow = _flow.asStateFlow()

    private val sourceStateCase: SourceStateCase by Inject.injectLazy()
    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val sourceCase: SourceStateCase by Inject.injectLazy()

    private var initJob: Job? = null
    private var loadPlayJob: Job? = null

    init {
        // 收藏的番才能迁移
        if (cartoonInfo.starTime != 0L) {
            init()
        }
    }

    // 1. 搜索目标番剧
    // 用所有源搜索番名，取一页内容，如果一页少于 3 个则往后找知道 3 个。
    // 按照最短编辑距离 - 源优先级两个维度排序
    // 取第一个
    private fun init() {
        initJob?.cancel()
        initJob = viewModelScope.launch {
            _flow.update {
                it.copy(
                    isLoadingCover = true,
                )
            }
            val bundle = sourceStateCase.awaitBundle()
            val res = defSources.map {
                bundle.search(it)
            }.filterIsInstance<SearchComponent>()
                .map { component ->
                    async {
                        val first = component.getFirstSearchKey(cartoonInfo.name)
                        val res = arrayListOf<CartoonCover>()
                        var currentKey: Int? = first
                        while (res.size < 3 && currentKey != null) {
                            component.search(currentKey, cartoonInfo.name)
                                .complete {
                                    yield()
                                    if(it.data.second.isNotEmpty()){
                                        res += it.data.second.subList(0, min(1, it.data.second.size))
                                    }
                                    currentKey = it.data.first
                                }
                                .error {
                                    yield()
                                    it.throwable.printStackTrace()
                                    currentKey = null
                                }
                        }
                        res
                    }
                }.map {
                    it.await()
                }.flatten()
                .let {
                    if (it.isEmpty()) {
                        null
                    } else {
                        val first = it.first()
                        var min = minDistance(first.title, cartoonInfo.name) to first
                        for (i in 1 until it.size) {
                            val cur = it[i]
                            val current = minDistance(cur.title, cartoonInfo.name) to cur
                            if (current.first < min.first) {
                                min = current
                            } else if (current.first == min.first && defSources.indexOf(current.second.source) < defSources.indexOf(
                                    min.second.source
                                )
                            ) {
                                min = current
                            }
                        }
                        min.second
                    }

                }
            if (res == null) {
                _flow.update {
                    it.copy(
                        isLoadingCover = false,
                        isLoadingPlay = false,
                        cartoonCover = null,
                    )
                }
            } else {
                _flow.update {
                    it.copy(
                        isLoadingCover = false,
                        cartoonCover = res,
                    )
                }
                changeCover(res)
            }

        }
    }

    // 2. 获取确定的目标番剧的播放线路，如果用户手动选择目标番剧可以重做这一步
    // 直接用源获取所有播放线路
    // 然后自动确定迁移进度，优先匹配集数一样的线路，集数优先匹配 order 字段
    fun changeCover(cartoonCover: CartoonCover) {
        loadPlayJob?.cancel()
        loadPlayJob = viewModelScope.launch {
            val bundle = sourceStateCase.awaitBundle()
            val detailed = bundle.detailed(cartoonCover.source)
            if (detailed == null) {
                _flow.update {
                    it.copy(
                        isLoadingPlay = false,
                        playLineList = emptyList(),
                    )
                }
                return@launch
            }
            _flow.update {
                it.copy(
                    cartoonCover = cartoonCover,
                    isLoadingPlay = true,
                    playLineList = emptyList(),
                )
            }
            detailed.getAll(
                CartoonSummary(
                    cartoonCover.id,
                    cartoonCover.source,
                )
            )
                .complete { complete ->
                    yield()
                    _flow.update {
                        it.copy(
                            isLoadingPlay = false,
                            playLineList = complete.data.second,
                            cartoon = complete.data.first
                        )
                    }
                    yield()
                    val oldPlayState = cartoonInfo.matchHistoryEpisode
                    if (oldPlayState == null) {
                        val first = complete.data.second.firstOrNull()
                        if (first != null) {
                            changeEpisode(
                                PlayLineWrapper.SORT_DEFAULT_KEY,
                                first,
                                first.episode.firstOrNull()
                            )
                        }
                    } else {
                        val playLine =
                            complete.data.second.find { it.episode.size == oldPlayState.first.playLine.episode.size }
                                ?: complete.data.second.firstOrNull()
                        if (playLine != null) {
                            val episode =
                                playLine.episode.find { it.order == oldPlayState.second.order }
                                    ?: playLine.episode.getOrNull(
                                        oldPlayState.first.playLine.episode.indexOf(
                                            oldPlayState.second
                                        )
                                    )
                            changeEpisode(PlayLineWrapper.SORT_DEFAULT_KEY, playLine, episode)
                        }
                    }
                }
                .error {
                    yield()
                    _flow.update {
                        it.copy(
                            isLoadingPlay = false,
                            playLineList = emptyList(),
                        )
                    }
                }
        }
    }

    // 3. 确定迁移后的播放进度
    // 如果用户手动选择可以重做这一步
    fun changeEpisode(sortKey: String, playLine: PlayLine, episode: Episode?) {
        val epi = episode ?: playLine.episode.firstOrNull()
        _flow.update {
            it.copy(
                sortKey = sortKey,
                playLine = playLine,
                episode = epi
            )
        }
    }

    fun migrate(
        onError: (String) -> Unit = { it.moeSnackBar() },
        onSus: () -> Unit,
    ) {
        viewModelScope.launch {
            val item = _flow.value
            val car = item.cartoon
            if (
                item.isMigrating || item.isLoadingCover || item.isLoadingPlay || item.isMigrated ||
                item.cartoonCover == null || car == null
            ) {
                if (car == null && !item.isLoadingPlay) onError("目标番剧详情不可用")
                return@launch
            }
            _flow.update {
                it.copy(
                    isMigrating = true
                )
            }

            val result = runCatching {
                val bundle = sourceCase.awaitBundle()
                val sourceName = bundle.source(car.source)?.label ?: ""
                val episodeList = item.playLineWrapper?.sortedEpisodeList
                val targetCartoon = CartoonInfo.fromCartoon(
                    car,
                    sourceName,
                    item.playLineList
                ).copy(
                    lastHistoryTime = cartoonInfo.lastHistoryTime,
                    lastPlayLineEpisodeString = episodeList?.toJson() ?: "",
                    lastLineId = item.playLine?.id ?: "",
                    lastLinesIndex = item.playLineList.indexOf(item.playLine),
                    lastLineLabel = item.playLine?.label ?: "",
                    lastEpisodeLabel = item.episode?.label ?: "",
                    lastEpisodeId = item.episode?.id ?: "",
                    lastEpisodeIndex = episodeList?.indexOf(item.episode) ?: -1,
                    lastEpisodeOrder = item.episode?.order ?: -1,
                    lastProcessTime = 0,
                )

                cartoonInfoDao.migrateStar(cartoonInfo.id, cartoonInfo.source, targetCartoon)
            }.getOrElse {
                _flow.update { state -> state.copy(isMigrating = false) }
                onError(it.message ?: "迁移失败")
                return@launch
            }

            val success = result == StarMigrationResult.SUCCESS
            _flow.update { it.copy(isMigrating = false, isMigrated = success) }
            if (success) {
                onSus()
            } else {
                onError(
                    when (result) {
                        StarMigrationResult.SOURCE_MISSING -> "原追番记录不存在"
                        StarMigrationResult.SOURCE_NOT_STARRED -> "原番剧已不在追番列表"
                        StarMigrationResult.TARGET_SAME -> "目标与原番剧相同，无需迁移"
                        StarMigrationResult.TARGET_CONFLICT -> "目标番剧已有记录，为避免覆盖已停止迁移"
                        StarMigrationResult.SUCCESS -> ""
                    }
                )
            }
        }

    }


    private fun minDistance(a: String, b: String): Int {
        val na = a.length
        val nb = b.length
        val dp = Array(na + 1) {
            Array(nb + 1) { 0 }
        }
        for (j in 1..nb) {
            dp[0][j] = dp[0][j - 1] + 1
        }
        for (i in 1..na) {
            dp[i][0] = dp[i - 1][0] + 1
        }
        for (i in 1..na) {
            for (j in 1..nb) {
                if (a[i - 1] == b[j - 1]) dp[i][j] = dp[i - 1][j - 1];
                else dp[i][j] = dp[i - 1][j - 1].coerceAtMost(dp[i][j - 1])
                    .coerceAtMost(dp[i - 1][j]) + 1
            }
        }
        return dp[na][nb]
    }


}

class MigrateItemViewModelFactory(
    private val cartoonInfo: CartoonInfo,
    private val defSources: List<String>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MigrateItemViewModel::class.java))
            return MigrateItemViewModel(cartoonInfo, defSources) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}
