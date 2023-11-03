package com.heyanle.easybangumi4.cartoon.play

import android.content.Intent
import androidx.core.net.toUri
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.entity.CartoonHistory
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.exo.EasyExoPlayer
import com.heyanle.easybangumi4.exo.MediaSourceFactory
import com.heyanle.easybangumi4.getter.SourceStateGetter
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * 番剧播放状态 controller，最终播放交给 exoplayer 这里是管理番，播放线路，哪一集以及解析
 * PlayComponent -> CartoonPlayingController -> ExoPlayer
 * Created by heyanlin on 2023/10/31.
 */
class CartoonPlayingController(
    private val settingPreference: SettingPreferences,
    private val sourceStateGetter: SourceStateGetter,
    private val cartoonHistoryDao: CartoonHistoryDao,
    private val mediaSourceFactory: MediaSourceFactory,
    private val exoPlayer: EasyExoPlayer,
) : Player.Listener {

    companion object {
        private const val TAG = "CartoonPlayingController"
        private const val EXOPLAYER_SCENE = TAG
    }

    sealed class PlayingState {

        data object Idle : PlayingState()

        data class Loading(
            val playLine: PlayLine,
            val episode: Episode,
            val cartoon: CartoonInfo,
        ) : PlayingState()

        data class Playing(
            val playerInfo: PlayerInfo,
            val playLine: PlayLine,
            val episode: Episode,
            val cartoon: CartoonInfo,
        ) : PlayingState()

        data class Error(
            val cartoon: CartoonInfo?,
            val errMsg: String,
            val throwable: Throwable?,
            val playLine: PlayLine,
            val episode: Episode,
        ) : PlayingState()

        fun cartoon(): CartoonInfo? {
            return when(this){
                is Loading -> cartoon
                is Playing -> cartoon
                is Error -> cartoon
                else -> null
            }
        }

        fun playLine(): PlayLine? {
            return when(this){
                is Loading -> playLine
                is Playing -> playLine
                is Error -> playLine
                else -> null
            }
        }

        fun episode(): Episode? {
            return when(this){
                is Loading -> episode
                is Playing -> episode
                is Error -> episode
                else -> null
            }
        }

    }

    private val _state = MutableStateFlow<PlayingState>(PlayingState.Idle)
    val state = _state.asStateFlow()

    private val scope = MainScope()
    private var lastChangeJob: Job? = null

    /**
     * 缓存一下正在播放的 PlayerInfo
     */
    private var currentPlayerInfo: PlayerInfo? = null

    /**
     * 切换播放的视频
     * @param cartoon 番剧
     * @param playLine 播放线路
     * @param episode 集
     * @param adviceProgress 跳转进度
     */
    fun changePlay(
        cartoon: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        adviceProgress: Long = 0L,
    ) {
        lastChangeJob?.cancel()
        lastChangeJob = scope.launch {
            innerChangePlay(cartoon, playLine, episode, adviceProgress)
        }
    }

    fun refresh(){
        val oldPlayingState = _state.value
        val cartoon = oldPlayingState.cartoon() ?: return
        val playLine = oldPlayingState.playLine() ?: return
        val episode = oldPlayingState.episode() ?: return
        changePlay(cartoon, playLine, episode)

    }

    /**
     * 调用外部播放器播放，当前状态为 Playing 时候才有效
     * @return 当前播放状态是否为 Playing
     */
    fun playCurrentExternal(): Boolean {
        val oldPlayingState = _state.value
        if(oldPlayingState is PlayingState.Playing ){
            innerPlayExternal(oldPlayingState.playerInfo)
            return true
        }
        return false
    }

    fun trySaveHistory(ps: Long = -1) {
        if (settingPreference.isInPrivate.get()) {
            return
        }
        var process = ps
        if (ps == -1L) {
            process = exoPlayer.currentPosition
        }
        if (process == -1L) {
            process = 0L
        }
        val oldPlayingState = _state.value
        if(oldPlayingState is PlayingState.Playing ){
            scope.launch(Dispatchers.IO) {
                val curCartoon = oldPlayingState.cartoon
                val history = CartoonHistory(
                    id = curCartoon.id,
                    source = curCartoon.source,
                    url = curCartoon.url,

                    cover = curCartoon.coverUrl ?: "",
                    name = curCartoon.title,
                    intro = curCartoon.intro ?: "",

                    lastLinesIndex = curCartoon.getPlayLine().indexOf(oldPlayingState.playLine),
                    lastLineTitle = oldPlayingState.playLine.label,
                    lastLineId = oldPlayingState.playLine.id,

                    lastEpisodeIndex = oldPlayingState.playLine.episode.indexOf(oldPlayingState.episode),
                    lastEpisodeTitle = oldPlayingState.episode.label,
                    lastEpisodeId = oldPlayingState.episode.id,
                    lastEpisodeOrder = oldPlayingState.episode.order,

                    lastProcessTime = process,

                    createTime = System.currentTimeMillis()
                )
                cartoonHistoryDao.modify(history)
            }
        }
    }

    fun tryNext(
        defaultProgress: Long = 0L,
        isReverse: Boolean = false,
    ): Boolean {
        val playingState = (state.value as? PlayingState.Playing) ?: return false
        val currentIndex = playingState.playLine.episode.indexOf(playingState.episode)
        val target = if (isReverse) currentIndex - 1 else currentIndex + 1
        if (target < 0 || target >= playingState.playLine.episode.size) {
            return false
        }
        changePlay(
            playingState.cartoon,
            playingState.playLine,
            playingState.playLine.episode[target],
            defaultProgress
        )
        return true
    }

    fun release() {
        lastChangeJob?.cancel()
        exoPlayer.stop()
    }


    private suspend fun CoroutineScope.innerChangePlay(
        cartoon: CartoonInfo,
        playLine: PlayLine,
        episode: Episode,
        adviceProgress: Long = 0L,
    ) {
        val oldPlayingState = _state.value
        if (oldPlayingState is PlayingState.Playing &&
            oldPlayingState.cartoon == cartoon &&
            oldPlayingState.playLine == playLine &&
            oldPlayingState.episode == episode
        ) {
            // 番剧 播放线路 集数都一致直接播放即可，不用解析了
            innerPlay(oldPlayingState.playerInfo, adviceProgress)
            // 可能业务需要触发一下刷新，这里 copy 一下刷一下界面
            _state.update {
                oldPlayingState.copy(
                    cartoon = cartoon,
                    playLine = playLine,
                    episode = episode,
                    playerInfo = oldPlayingState.playerInfo
                )
            }
            return
        }
        _state.update {
            PlayingState.Loading(
                playLine, episode, cartoon
            )
        }
        // 先暂停播放
        exoPlayer.pause()
        val playComponent = sourceStateGetter.awaitBundle().play(cartoon.source)
        yield() // 给个 cancel 时点
        if (playComponent == null) {
            _state.update {
                PlayingState.Error(
                    cartoon = cartoon,
                    errMsg = stringRes(com.heyanle.easy_i18n.R.string.source_error),
                    throwable = null,
                    playLine = playLine,
                    episode = episode
                )
            }
            return
        }
        playComponent.getPlayInfo(
            CartoonSummary(cartoon.id, cartoon.source, cartoon.url),
            playLine,
            episode
        )
            .complete { complete ->
                yield()
                if (isActive) {
                    innerPlay(complete.data, adviceProgress)
                    _state.update {
                        PlayingState.Playing(
                            cartoon = cartoon,
                            playLine = playLine,
                            episode = episode,
                            playerInfo = complete.data
                        )
                    }
                }
            }
            .error { error ->
                yield()
                if (isActive) {
                    _state.update {
                        PlayingState.Error(
                            cartoon = cartoon,
                            errMsg = if (error.isParserError) error.throwable.message
                                ?: "" else stringRes(com.heyanle.easy_i18n.R.string.source_error),
                            throwable = null,
                            playLine = playLine,
                            episode = episode
                        )
                    }
                }
            }

    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun innerPlay(playerInfo: PlayerInfo, adviceProgress: Long = 0L) {
        if (settingPreference.useExternalVideoPlayer.get()) {
            innerPlayExternal(playerInfo)
            return
        }
        exoPlayer.mediaMetadata
        // 如果播放器当前状态不在播放 || exoplayer 被其他业务使用过 || 缓存的上一个 PlayerInfo 与新的不一致
        // 则重新加载
        if (!exoPlayer.isMedia() ||
            exoPlayer.scene != EXOPLAYER_SCENE ||
            currentPlayerInfo?.uri != playerInfo.uri ||
            currentPlayerInfo?.decodeType != playerInfo.decodeType
        ) {
            val media = mediaSourceFactory.get(playerInfo)
            exoPlayer.setMediaSource(media, adviceProgress)
            exoPlayer.prepare(EXOPLAYER_SCENE)
            exoPlayer.playWhenReady = true
        } else {
            // 已经在播放同一部，直接 seekTo 对应 progress
            exoPlayer.seekTo(adviceProgress)
            exoPlayer.playWhenReady = true
        }
    }

    /**
     * 调用外部播放器播放
     */
    private fun innerPlayExternal(playerInfo: PlayerInfo) {
        APP.startActivity(Intent("android.intent.action.VIEW").apply {
            setDataAndType(playerInfo.uri.toUri(), "video/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // for mx player https://mx.j2inter.com/api
            putExtra("video_list", arrayOf(playerInfo.uri.toUri()))
            val list = arrayListOf<String>()
            playerInfo.header?.iterator()?.forEach {
                list.add(it.key)
                list.add(it.value)
            }
            putExtra("headers", list.toTypedArray())
        })
    }

    private fun ExoPlayer.isMedia(): Boolean {
        return playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY
    }

}