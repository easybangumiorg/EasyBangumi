package com.heyanle.easybangumi.ui.player

import android.util.Log
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.utils.stringRes
import com.heyanle.lib_anim.entity.BangumiSummary
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/11 16:36.
 * https://github.com/heyanLE
 */
class AnimPlayItemController(
    val bangumiSummary: BangumiSummary,
) {

    val scope = MainScope()

    // 储存 线路与集数的对应关系，只是用于切换线路的时候缓存一下当前选择
    val curEpisode: HashMap<Int, Int> = hashMapOf()

    // == 储存当前播放数据与播放的线路集数 =============================
    // == 用于播放器里选集

    val curPlayMsg: LinkedHashMap<String, List<String>> = linkedMapOf()
    var curPlay: Pair<Int, Int> = 0 to 0

    val detailController = DetailController(bangumiSummary)
    val playMsgController = PlayMsgController(bangumiSummary)


    // 播放 Url 真正播放器的状态由 EasyPlayer 自己维护
    sealed class PlayerStatus(
        val sourceIndex: Int, val episode: Int
    ) {
        class None(sourceIndex: Int, episode: Int) : PlayerStatus(sourceIndex, episode)

        class Loading(sourceIndex: Int, episode: Int) : PlayerStatus(sourceIndex, episode)
        class Play(sourceIndex: Int, episode: Int, val uri: String, val type: Int = 0) :
            PlayerStatus(sourceIndex, episode) {

        }

        class Error(
            sourceIndex: Int,
            episode: Int,
            val errorMsg: String,
            val throwable: Throwable?
        ) : PlayerStatus(sourceIndex, episode)
    }


    // 事件
    sealed class PlayerEvent {
        class None(val lineIndex: Int, val episode: Int) : PlayerEvent()
        class ChangePlay(val lineIndex: Int, val episode: Int) : PlayerEvent()

        class ChangeLine(val lineIndex: Int) : PlayerEvent()
    }


    private val eventFlow = MutableStateFlow<PlayerEvent>(
        PlayerEvent.None(0, 0)
    )

    private val _playerStatus = channelFlow<PlayerStatus> {
        eventFlow.collectLatest { event ->
            when (event) {
                is PlayerEvent.None -> {
                    send(PlayerStatus.None(event.lineIndex, event.episode))
                }
                is PlayerEvent.ChangePlay -> {
                    curEpisode[event.lineIndex] = event.episode
                    send(PlayerStatus.Loading(event.lineIndex, event.episode))
                    kotlin.runCatching {
                        val res = AnimSourceFactory.play(bangumiSummary.source)

                        res?.getPlayUrl(bangumiSummary, event.lineIndex, event.episode)?.complete {
                            send(
                                PlayerStatus.Play(
                                    event.lineIndex,
                                    event.episode,
                                    it.data.uri,
                                    it.data.type
                                )
                            )
                        }?.error {
                            send(
                                PlayerStatus.Error(
                                    event.lineIndex, event.episode, if (it.isParserError) stringRes(
                                        R.string.source_error
                                    ) else stringRes(R.string.loading_error), it.throwable
                                )
                            )
                        }
                            ?: send(
                                PlayerStatus.Error(
                                    event.lineIndex, event.episode, stringRes(
                                        R.string.source_not_found
                                    ), java.lang.NullPointerException("source Not found")
                                )
                            )

                    }.onFailure {
                        it.printStackTrace()
                        send(
                            PlayerStatus.Error(
                                event.lineIndex,
                                event.episode,
                                stringRes(R.string.loading_error),
                                it
                            )
                        )
                    }

                }
                is PlayerEvent.ChangeLine -> {
                    eventFlow.emit(
                        PlayerEvent.ChangePlay(
                            event.lineIndex,
                            curEpisode.get(event.lineIndex) ?: 0
                        )
                    )
                }
            }
        }
    }
    val playerStatus: Flow<PlayerStatus> = _playerStatus

    init {
        scope.launch {
            playMsgController.flow.collectLatest {

                if (it is PlayMsgController.PlayMsgStatus.Completely) {

//                    val state = playerStatus.last()
//                    Log.d("AnimPlayItemController", "$it")
//                    val lineIndex = state.sourceIndex ?:0
//                    val episode = state.episode ?:0

                    Log.d("AnimPlayItemController", "$it")
                    curPlayMsg.clear()
                    curPlayMsg.putAll(it.playMsg)

                    //eventFlow.emit(PlayerEvent.ChangePlay(lineIndex, episode))
                }
            }
        }
        scope.launch {
            playerStatus.collectLatest { status ->
                (status as? PlayerStatus.Play)?.let {
                    curPlay = it.sourceIndex to it.episode
                }
            }
        }
    }

    fun load() {
        scope.launch {
            detailController.load()
            playMsgController.load()
        }
    }

    fun changeLines(lineIndex: Int) {
        Log.d("AnimPlayItemController", "changeLines lineIndex->$lineIndex")
        scope.launch {
            eventFlow.emit(PlayerEvent.ChangeLine(lineIndex))
        }
    }

    fun changePlayer(lineIndex: Int, episode: Int) {

        scope.launch {
            eventFlow.emit(PlayerEvent.ChangePlay(lineIndex, episode))

        }
    }

    fun tryNext(): Boolean {
        if (curPlayMsg.isEmpty()) {
            return false
        }
        val line = curPlay.first
        val epi = curPlay.second + 1
        val lines = curPlayMsg.keys.toList()
        if (line < 0 || line >= lines.size) {
            return false
        }
        val key = lines[line]
        val es = curPlayMsg[key] ?: return false
        if (epi < 0 || epi >= es.size) {
            return false
        }
        scope.launch {
            eventFlow.emit(PlayerEvent.ChangePlay(line, epi))
        }
        return true
    }

    fun clear() {
        scope.cancel()
    }
}

