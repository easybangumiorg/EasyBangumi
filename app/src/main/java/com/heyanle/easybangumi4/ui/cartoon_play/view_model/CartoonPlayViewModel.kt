package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class CartoonPlayViewModel(
    private var enter: EnterData? = null,
): ViewModel() {


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
    ){
        fun isEffective(): Boolean {
            return playLineId.isNotEmpty() || playLineLabel.isNotEmpty() || playLineIndex >= 0
                    || episodeId.isNotEmpty() || episodeLabel.isNotEmpty() || episodeOrder >= 0 || episodeIndex >= 0
                    || adviceProgress > -1
        }
    }


    data class CartoonPlayState(
        val cartoonSummary: CartoonSummary,
        val playLine: PlayLineWrapper,
        val episode: Episode,
    ){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CartoonPlayState

            if (cartoonSummary != other.cartoonSummary) return false
            if (playLine != other.playLine) return false
            return episode == other.episode
        }

        override fun hashCode(): Int {
            var result = cartoonSummary.hashCode()
            result = 31 * result + playLine.hashCode()
            result = 31 * result + episode.hashCode()
            return result
        }
    }


    var selectedLineIndex by mutableIntStateOf(0)

    private val _curringPlayStatus = MutableStateFlow<CartoonPlayState?>(null)
    val curringPlayState = _curringPlayStatus.asStateFlow()

    var adviceProgress: Long = -1L

    fun onCartoonInfoChange(
        info: CartoonInfo
    ){
        val old = _curringPlayStatus.value
        if(old != null && old.cartoonSummary == info.toSummary()){
            return
        }
        val pair = if(enter == null || enter?.isEffective() != true){
            if(adviceProgress == -1L && old == null){
                adviceProgress = info.lastProcessTime
            }
            info.matchHistoryEpisode
        }else{
            if(adviceProgress == -1L && old == null){
                adviceProgress = enter?.adviceProgress?:0L
            }
            match(info.playLineWrapper, enter)
        }

        // enter 只生效一次
        enter = null

        _curringPlayStatus.update {
            if(pair != null){
                CartoonPlayState(info.toSummary(), pair.first, pair.second)
            }else{
                null
            }
        }
    }

    fun changePlay(
        cartoonSummary: CartoonSummary,
        playLineWrapper: PlayLineWrapper,
        episode: Episode,
    ){
        _curringPlayStatus.update {
            CartoonPlayState(cartoonSummary, playLineWrapper, episode)
        }
    }
    fun changePlay(
        cartoonInfo: CartoonInfo,
        playLineWrapper: PlayLineWrapper,
        episode: Episode,
    ){
        _curringPlayStatus.update {
            CartoonPlayState(cartoonInfo.toSummary(), playLineWrapper, episode)
        }
    }

    fun tryNext(){
        val current = _curringPlayStatus.value ?: return
        val index = current.playLine.sortedEpisodeList.indexOf(current.episode)  + 1
        if(index <= 0|| index >= current.playLine.sortedEpisodeList.size){
            return
        }
        _curringPlayStatus.update {
            CartoonPlayState(current.cartoonSummary,  current.playLine, current.playLine.sortedEpisodeList[index])
        }

    }

    private fun match(
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