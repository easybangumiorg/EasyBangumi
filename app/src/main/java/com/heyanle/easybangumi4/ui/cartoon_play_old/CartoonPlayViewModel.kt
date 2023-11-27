package com.heyanle.easybangumi4.ui.cartoon_play_old

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.play.CartoonPlayingControllerOld
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by heyanlin on 2023/10/31.
 */
class CartoonPlayViewModel : ViewModel() {

    /**
     * 匹配播放线路 -> 匹配集数 -> 跳转进度
     * 否则尝试获取历史记录中的 EnterData 进行匹配
     * 否则播放第一条线路第一集
     *
     * 线路匹配顺序 id -> label -> index
     * 集匹配线路 id -> order -> label -> index
     *
     * 对于字符串为空则未指定，数字 < 0 则未指定
     */
    data class EnterData(
        val playLineId: String,
        val playLineLabel: String,
        val playLineIndex: Int,

        val episodeId: String,
        val episodeLabel: String,
        val episodeOrder: Int,
        val episodeIndex: Int,

        val adviceProgress: Long,
    )

    var selectedLineIndex by mutableIntStateOf(0)

    private val cartoonHistoryDao: CartoonHistoryDao by Injekt.injectLazy()
    private val cartoonPlayingControllerOld: CartoonPlayingControllerOld by Injekt.injectLazy()


    fun onDetailedLoaded(
        info: DetailedViewModelOld.DetailedState.Info,
        enter: EnterData?,
    ) {
        viewModelScope.launch {
            var adviceProgress = enter?.adviceProgress ?: 0L
            // 直接 match
            var matchPlayItem: Pair<PlayLine, Episode>? = null
            if (enter != null) {
                matchPlayItem = match(info, enter)
            }
            if (matchPlayItem == null) {
                val data = getEnterDataFromHistory(info.detail.getSummary())
                adviceProgress = data.adviceProgress
                matchPlayItem = match(info, data)
            }
            if (matchPlayItem == null) {
                adviceProgress = 0L
                matchPlayItem = match(info, null)
            }
            val realItem = matchPlayItem ?: return@launch
            cartoonPlayingControllerOld.changePlay(
                info.detail,
                realItem.first,
                realItem.second,
                adviceProgress.coerceAtLeast(0)
            )
        }

    }

    private fun match(
        info: DetailedViewModelOld.DetailedState.Info,
        enter: EnterData?
    ): Pair<PlayLine, Episode>? {
        // enter 为 null 默认命中第一条线路第一集
        if (enter == null) {
            val playLine = info.playLine.firstOrNull() ?: return null
            val episode = playLine.episode.firstOrNull() ?: return null
            return playLine to episode
        }
        /**
         * 使用状态压缩进行优先级匹配，o(n) 即可
         */
        var currentPlayLine: PlayLine? = null
        var currentPlayLineMask: Int = 0
        info.playLine.forEachIndexed { index, playLine ->
            var mask = 0
            if (enter.playLineId.isNotEmpty() && enter.playLineId == playLine.id) {
                mask = mask or 0b100
            }
            if (enter.playLineLabel.isNotEmpty() && enter.playLineLabel == playLine.label) {
                mask = mask or 0b010
            }
            if (enter.playLineIndex >= 0 && enter.playLineIndex == index) {
                mask = mask or 0b001
            }
            if (mask > currentPlayLineMask) {
                currentPlayLine = playLine
                currentPlayLineMask = mask
            }
        }

        // 匹配不到播放线路直接返回 null，使用降级（历史记录或兜底）
        if (currentPlayLine == null) {
            return null
        }
        var currentEpisode: Episode? = null
        var currentEpisodeMask = 0
        currentPlayLine?.episode?.forEachIndexed { index, episode ->
            var mask = 0
            if (enter.episodeId.isNotEmpty() && enter.episodeId == episode.id) {
                mask = mask or 0b1000
            }
            if (enter.episodeOrder >= 0 && enter.episodeOrder == episode.order) {
                mask = mask or 0b0100
            }
            if (enter.episodeLabel.isNotEmpty() && enter.episodeLabel == episode.label) {
                mask = mask or 0b0010
            }
            if (enter.episodeIndex >= 0 && enter.episodeIndex == index) {
                mask = mask or 0b0001
            }
            if (mask > currentEpisodeMask) {
                currentEpisode = episode
                currentEpisodeMask = mask
            }

        }
        return (currentPlayLine ?: return null) to (currentEpisode ?: return null)
    }

    private suspend fun getEnterDataFromHistory(cartoonSummary: CartoonSummary): EnterData {
        return withContext(Dispatchers.IO) {
            val hist = cartoonHistoryDao.getFromCartoonSummary(
                cartoonSummary.id,
                cartoonSummary.source,
                cartoonSummary.url,
            )
            return@withContext EnterData(
                playLineId = hist?.lastLineId ?: "",
                playLineLabel = hist?.lastLineTitle ?: "",
                playLineIndex = hist?.lastLinesIndex ?: -1,

                episodeId = hist?.lastEpisodeId ?: "",
                episodeLabel = hist?.lastLineTitle ?: "",
                episodeOrder = hist?.lastEpisodeOrder ?: -1,
                episodeIndex = hist?.lastEpisodeIndex ?: -1,

                adviceProgress = hist?.lastProcessTime ?: 0L
            )
        }
    }

}