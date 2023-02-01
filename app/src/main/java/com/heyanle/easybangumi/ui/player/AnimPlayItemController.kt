package com.heyanle.easybangumi.ui.player

import android.util.Log
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.db.EasyDB
import com.heyanle.easybangumi.db.entity.BangumiHistory
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.ui.home.history.AnimHistoryViewModel
import com.heyanle.easybangumi.utils.stringRes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Created by HeYanLe on 2023/1/11 16:36.
 * https://github.com/heyanLE
 */
class AnimPlayItemController(
    val bangumiSummary: BangumiSummary,
    var enterData: BangumiPlayController.EnterData? = null,
) {

    val scope = MainScope()

    // 储存 线路与集数的对应关系，只是用于切换线路的时候缓存一下当前选择
    val curEpisode: HashMap<Int, Int> = hashMapOf()

//    // == 储存当前播放数据与播放的线路集数 =============================
//    // == 用于播放器里选集
//
//    val curPlayMsg: LinkedHashMap<String, List<String>> = linkedMapOf()
//    var curPlay: Pair<Int, Int> = 0 to 0

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
        object None : PlayerEvent()
        class ChangePlay(val lineIndex: Int, val episode: Int) : PlayerEvent()
        class ChangeLine(val lineIndex: Int) : PlayerEvent()
    }

    private val eventFlow = MutableStateFlow<PlayerEvent>(
        PlayerEvent.None
    )

    private val _playerStatus = MutableStateFlow<PlayerStatus>(
        PlayerStatus.None(
            enterData?.sourceIndex ?: -1,
            enterData?.episode ?: -1
        )
    )
    val playerStatus: StateFlow<PlayerStatus> = _playerStatus

    init {
        scope.launch {
            playMsgController.init()
        }
        scope.launch {
            detailController.init()
        }
        scope.launch {
            eventFlow.collectLatest { event ->
                when (event) {
                    is PlayerEvent.None -> {
                        _playerStatus.emit(
                            PlayerStatus.None(
                                enterData?.sourceIndex ?: -1,
                                enterData?.episode ?: -1
                            )
                        )
                    }
                    is PlayerEvent.ChangePlay -> {
                        curEpisode[event.lineIndex] = event.episode
                        _playerStatus.emit(PlayerStatus.Loading(event.lineIndex, event.episode))
                        kotlin.runCatching {
                            val res = AnimSourceFactory.play(bangumiSummary.source)

                            res?.getPlayUrl(bangumiSummary, event.lineIndex, event.episode)
                                ?.complete {
                                    _playerStatus.emit(
                                        PlayerStatus.Play(
                                            event.lineIndex,
                                            event.episode,
                                            it.data.uri,
                                            it.data.type
                                        )
                                    )
                                }?.error {
                                    it.throwable.printStackTrace()
                                    _playerStatus.emit(

                                        PlayerStatus.Error(
                                            event.lineIndex,
                                            event.episode,
                                            if (it.isParserError) stringRes(
                                                R.string.source_error
                                            ) else stringRes(R.string.loading_error),
                                            it.throwable
                                        )
                                    )
                                }
                                ?: _playerStatus.emit(
                                    PlayerStatus.Error(
                                        event.lineIndex, event.episode, stringRes(
                                            R.string.source_not_found
                                        ), java.lang.NullPointerException("source Not found")
                                    )
                                )

                        }.onFailure {
                            it.printStackTrace()
                            _playerStatus.emit(
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
                                curEpisode[event.lineIndex] ?: 0
                            )
                        )
                    }
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
            if(lineIndex == -1){

                eventFlow.emit(PlayerEvent.ChangePlay(0, 0))
            }else if(episode == -1){
                eventFlow.emit(PlayerEvent.ChangePlay(lineIndex, 0))
            }else{
                eventFlow.emit(PlayerEvent.ChangePlay(lineIndex, episode))
            }



        }
    }


    // 当 番剧详情 和 番剧播放列表 都加载完 并且 播放状态为 None 时第一个调用
    fun onShow(lineIndex: Int, episode: Int) {

        if (lineIndex != -1 && episode != -1) {
            changePlayer(lineIndex, episode)
            return
        }
        scope.launch {
            val history = withContext(Dispatchers.IO) {
                EasyDB.database.bangumiHistory.getFromBangumiSummary(
                    bangumiSummary.source,
                    bangumiSummary.detailUrl
                )
            }
            Log.d("AnimPlayItemController", "onShow $history")
            if (history == null) {
                eventFlow.emit(
                    PlayerEvent.ChangePlay(
                        if (lineIndex == -1) 0 else lineIndex,
                        if (episode == -1) 0 else episode
                    )
                )
            } else {
                if (lineIndex == -1) {
                    eventFlow.emit(
                        PlayerEvent.ChangePlay(
                            history.lastLinesIndex,
                            history.lastEpisodeIndex
                        )
                    )
                } else {
                    eventFlow.emit(PlayerEvent.ChangePlay(lineIndex, 0))
                }
            }
        }
    }

    fun onNewEnter(enterData: BangumiPlayController.EnterData?) {
        Log.d("AnimPlayItemController", "onNewEnter ${enterData}")
        this.enterData = enterData
        if (enterData != null) {
            if(_playerStatus.value !is PlayerStatus.None){
                onShow(enterData.sourceIndex, enterData.episode)
            }
        }
    }

    private fun checkPlay(line: Int, epi: Int): Boolean {
        val status = playMsgController.flow.value as? PlayMsgController.PlayMsgStatus.Completely
            ?: return false
        val curPlayMsg = status.playMsg
        if (curPlayMsg.isEmpty()) {
            return false
        }
        val lines = curPlayMsg.keys.toList()
        if (line < 0 || line >= lines.size) {
            return false
        }
        val key = lines[line]
        val es = curPlayMsg[key] ?: return false
        if (epi < 0 || epi >= es.size) {
            return false
        }
        return true
    }

    fun getCurTitle(): String {
        val status =
            playMsgController.flow.value as? PlayMsgController.PlayMsgStatus.Completely ?: return ""
        val curPlayMsg = status.playMsg
        val curLine = playerStatus.value.sourceIndex
        val curEp = playerStatus.value.episode
        if (curPlayMsg.isEmpty()) {
            return ""
        }
        val lines = curPlayMsg.keys.toList()
        if (curLine < 0 || curLine >= lines.size) {
            return ""
        }
        val key = lines[curLine]
        val eps = curPlayMsg[key] ?: return ""
        if (curEp < 0 || curEp >= eps.size) {
            return ""
        }
        return eps[curEp]
    }

    fun getCurPlayList(): List<String> {

        val res = arrayListOf<String>()
        val status = playMsgController.flow.value as? PlayMsgController.PlayMsgStatus.Completely
            ?: return res
        val curPlayMsg = status.playMsg
        val curLine = playerStatus.value.sourceIndex
        if (curPlayMsg.isEmpty()) {
            return res
        }
        val lines = curPlayMsg.keys.toList()
        if (curLine < 0 || curLine >= lines.size) {
            return res
        }
        curPlayMsg[lines[curLine]]?.forEach {
            res.add(it)
        }
        return res
    }

    fun replay(): Boolean {
        val line = playerStatus.value.sourceIndex
        val epi = playerStatus.value.episode
        if (checkPlay(line, epi)) {
            scope.launch {
                eventFlow.emit(PlayerEvent.ChangePlay(line, epi))
            }
            return true
        }
        return false
    }

    fun tryNext(): Boolean {
        val line = playerStatus.value.sourceIndex
        val epi = playerStatus.value.episode + 1
        if (checkPlay(line, epi)) {
            scope.launch {
                eventFlow.emit(PlayerEvent.ChangePlay(line, epi))
            }
            return true
        }
        return false
    }

    fun onDispose(){
        BangumiPlayController.trySaveHistory(-1)
    }

    fun changeEpisode(epi: Int): Boolean {
        val line = playerStatus.value.sourceIndex
        if (checkPlay(line, epi)) {
            scope.launch {
                eventFlow.emit(PlayerEvent.ChangePlay(line, epi))
            }
            return true
        }
        return false
    }

    fun clear() {
        scope.cancel()
    }
}

