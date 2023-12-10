package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.play.CartoonPlayingController
import com.heyanle.easybangumi4.cartoon.play.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by heyanlin on 2023/10/31.
 */
class CartoonPlayViewModel(
    private var enter: EnterData?,
) : ViewModel() {

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
    private val cartoonPlayingController: CartoonPlayingController by Injekt.injectLazy()


    fun onDetailedChange(
        info: CartoonInfo,
        playLineWrapper: List<PlayLineWrapper>,
    ) {
        viewModelScope.launch {

            val current = cartoonPlayingController.state.value
            if(enter == null && current is CartoonPlayingController.PlayingState.Playing){
                val oldPlayLine = current.playLine.playLine
                val newPlayLine = playLineWrapper.find { it.playLine == oldPlayLine }
                if(newPlayLine != null){
                    cartoonPlayingController.changePlay(
                        info,
                        newPlayLine
                    )
                    return@launch
                }
            }

            var adviceProgress = enter?.adviceProgress ?: 0L
            // 直接 match
            var matchPlayItem: Pair<PlayLineWrapper, Episode>? = null
            if (enter != null) {
                matchPlayItem = match(info, playLineWrapper, enter)
            }
            if (matchPlayItem == null) {
                val data = getEnterDataFromHistory(info.getSummary())
                adviceProgress = data.adviceProgress
                matchPlayItem = match(info, playLineWrapper, data)
            }
            if (matchPlayItem == null) {
                adviceProgress = 0L
                matchPlayItem = match(info, playLineWrapper, null)
            }
            val realItem = matchPlayItem ?: return@launch
            cartoonPlayingController.changePlay(
                info,
                realItem.first,
                realItem.second,
                adviceProgress.coerceAtLeast(0)
            )
            enter = null
        }

    }

    private fun match(
        info: CartoonInfo,
        playLineWrapper: List<PlayLineWrapper>,
        enter: EnterData?
    ): Pair<PlayLineWrapper, Episode>? {
        // enter 为 null 默认命中第一条线路第一集
        if (enter == null) {
            val playLine = playLineWrapper.firstOrNull() ?: return null
            val episode = playLine.playLine.episode.firstOrNull() ?: return null
            return playLine to episode
        }
        /**
         * 使用状态压缩进行优先级匹配，o(n) 即可
         */
        var currentPlayLine: PlayLineWrapper? = null
        var currentPlayLineMask: Int = 0
        playLineWrapper.forEachIndexed { index, playLine ->
            var mask = 0
            if (enter.playLineId.isNotEmpty() && enter.playLineId == playLine.playLine.id) {
                mask = mask or 0b100
            }
            if (enter.playLineLabel.isNotEmpty() && enter.playLineLabel == playLine.playLine.label) {
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
        currentPlayLine?.playLine?.episode?.forEachIndexed { index, episode ->
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

class CartoonPlayViewModelFactory(
    private val enterData: CartoonPlayViewModel.EnterData?,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartoonPlayViewModel::class.java))
            return CartoonPlayViewModel(enterData) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}