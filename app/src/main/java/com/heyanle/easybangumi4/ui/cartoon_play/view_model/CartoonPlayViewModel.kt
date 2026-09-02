package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.utils.logi
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

        // 播放变体偏好：null=默认（有本地即本地）；true=本地优先；false=云端优先。
        // 旧路由数据缺失该字段时 Gson 反序列化为 null。
        val preferLocal: Boolean? = null,
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
        val cartoon: Cartoon? = null,
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


    /**
     * UI 正在浏览的播放线路。使用源维护的线路 id 作为身份，避免详情刷新或排序时
     * [PlayLineWrapper] 被重建后丢失选择。
     */
    var selectedLineId: String? = null
        private set

    /**
     * 兼容旧播放页的 index API。内部仍以 [selectedLineId] 为准，index 只作为当前列表
     * 无法解析 id 时的降级值。
     */
    private var legacySelectedLineIndex by mutableIntStateOf(0)
    var selectedLineIndex: Int
        get() = resolveSelectedLineIndex(latestPlayLines)
        set(value) {
            selectLine(latestPlayLines, value)
        }

    /** 最近一次详情快照生成的线路，排序变化后会替换为新的 wrapper。 */
    private var latestPlayLines: List<PlayLineWrapper> = emptyList()

    private val _curringPlayStatus = MutableStateFlow<CartoonPlayState?>(null)
    val curringPlayState = _curringPlayStatus.asStateFlow()

    var adviceProgress: Long = -1L

    fun onCartoonInfoChange(
        info: CartoonInfo
    ) {
        onCartoonInfoChange(info, info.playLineWrapper, traceEnabled = true)
    }

    /**
     * 接收详情与其同一时刻生成的线路快照。internal 重载也让纯 JVM 测试无需初始化
     * 全局 JSON/依赖注入环境即可验证播放状态迁移。
     */
    internal fun onCartoonInfoChange(
        info: CartoonInfo,
        playLines: List<PlayLineWrapper>,
        traceEnabled: Boolean = false,
    ){
        val old = _curringPlayStatus.value
        latestPlayLines = playLines
        if(old != null && old.cartoonSummary == info.toSummary()){
            reconcileSelectedLine(
                preferredLineId = selectedLineId,
                playingLineId = old.playLine.playLine.id,
            )
            if (traceEnabled) {
                "play-state action=ignore-same-cartoon previousId=${old.cartoonSummary.id} nextId=${info.id}".logi("PlaybackTrace")
            }
            return
        }
        if (traceEnabled) {
            "play-state action=cartoon-info previousId=${old?.cartoonSummary?.id} nextSource=${info.source} nextId=${info.id} title=${info.name}".logi("PlaybackTrace")
        }
        val pair = if(enter == null || enter?.isEffective() != true){
            if(adviceProgress == -1L && old == null){
                adviceProgress = info.lastProcessTime
            }
            matchHistory(playLines, info)
        }else{
            if(adviceProgress == -1L && old == null){
                adviceProgress = enter?.adviceProgress?:0L
            }
            match(playLines, enter)
        }

        // enter 只生效一次
        enter = null

        _curringPlayStatus.update {
            if(pair != null){
                if (traceEnabled) {
                    "play-state action=select source=${info.source} cartoonId=${info.id} lineId=${pair.first.playLine.id} episodeId=${pair.second.id}".logi("PlaybackTrace")
                }
                CartoonPlayState(info.toSummary(), pair.first, pair.second, info.toCartoon())
            }else{
                null
            }
        }
        reconcileSelectedLine(
            preferredLineId = pair?.first?.playLine?.id,
            playingLineId = pair?.first?.playLine?.id,
        )
    }

    fun changePlay(
        cartoonSummary: CartoonSummary,
        playLineWrapper: PlayLineWrapper,
        episode: Episode,
    ){
        selectLineById(playLineWrapper.playLine.id)
        _curringPlayStatus.update {
            CartoonPlayState(cartoonSummary, playLineWrapper, episode)
        }
    }
    fun changePlay(
        cartoonInfo: CartoonInfo,
        playLineWrapper: PlayLineWrapper,
        episode: Episode,
    ){
        selectLineById(playLineWrapper.playLine.id)
        _curringPlayStatus.update {
            CartoonPlayState(cartoonInfo.toSummary(), playLineWrapper, episode, cartoonInfo.toCartoon())
        }
    }

    fun tryNext(){
        val current = _curringPlayStatus.value ?: return
        val latestLine = latestPlayLines.firstOrNull {
            it.playLine.id == current.playLine.playLine.id
        } ?: current.playLine
        val episodes = latestLine.sortedEpisodeList
        val currentIndex = episodes.indexOfFirst { it.id == current.episode.id }
        val nextIndex = currentIndex + 1
        if(currentIndex < 0 || nextIndex >= episodes.size){
            return
        }
        selectLineById(latestLine.playLine.id)
        _curringPlayStatus.update {
            CartoonPlayState(
                current.cartoonSummary,
                latestLine,
                episodes[nextIndex],
                current.cartoon,
            )
        }

    }

    /** 根据稳定线路 id，在调用方持有的最新列表中解析 UI index。 */
    fun resolveSelectedLineIndex(playLines: List<PlayLineWrapper>): Int {
        val fallbackIndex = legacySelectedLineIndex
        if (playLines.isEmpty()) return fallbackIndex.coerceAtLeast(0)
        val selectedIndex = selectedLineId?.let { lineId ->
            playLines.indexOfFirst { it.playLine.id == lineId }.takeIf { it >= 0 }
        }
        return selectedIndex ?: fallbackIndex.coerceIn(playLines.indices)
    }

    /** 使用调用方的最新列表选择浏览线路，供新旧播放页共同使用。 */
    fun selectLine(playLines: List<PlayLineWrapper>, index: Int) {
        latestPlayLines = playLines
        val line = playLines.getOrNull(index) ?: return
        selectedLineId = line.playLine.id
        legacySelectedLineIndex = index
    }

    private fun selectLineById(lineId: String) {
        selectedLineId = lineId
        val index = latestPlayLines.indexOfFirst { it.playLine.id == lineId }
        if (index >= 0) legacySelectedLineIndex = index
    }

    private fun reconcileSelectedLine(
        preferredLineId: String?,
        playingLineId: String?,
    ) {
        val resolvedId = sequenceOf(preferredLineId, playingLineId)
            .filterNotNull()
            .firstOrNull { lineId -> latestPlayLines.any { it.playLine.id == lineId } }
            ?: latestPlayLines.firstOrNull()?.playLine?.id
        if (resolvedId == null) {
            selectedLineId = null
            legacySelectedLineIndex = 0
            return
        }
        selectLineById(resolvedId)
    }

    private fun matchHistory(
        playLines: List<PlayLineWrapper>,
        info: CartoonInfo,
    ): Pair<PlayLineWrapper, Episode>? {
        return match(
            playLines,
            EnterData(
                playLineId = info.lastLineId,
                playLineLabel = info.lastLineLabel,
                playLineIndex = info.lastLinesIndex,
                episodeId = info.lastEpisodeId,
                episodeLabel = info.lastEpisodeLabel,
                episodeOrder = info.lastEpisodeOrder,
                episodeIndex = info.lastEpisodeIndex,
                adviceProgress = info.lastProcessTime,
            ),
        )
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
