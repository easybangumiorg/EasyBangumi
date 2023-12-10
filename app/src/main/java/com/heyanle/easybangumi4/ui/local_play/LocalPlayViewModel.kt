package com.heyanle.easybangumi4.ui.local_play

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon_download.entity.LocalCartoon
import com.heyanle.easybangumi4.cartoon_download.entity.LocalEpisode
import com.heyanle.easybangumi4.exo.EasyExoPlayer
import com.heyanle.easybangumi4.case.LocalCartoonCase
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.cartoon_play.DetailedViewModel
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2023/9/25.
 */
class LocalPlayViewModel(
    private val uuid: String,
) : ViewModel(), Player.Listener {

    companion object {
        const val TAG = "LocalPlayViewModel"
        const val SORT_DEFAULT_KEY = "default"
    }

    val sortByDefault: SortBy<LocalEpisode> = SortBy<LocalEpisode>(
        SORT_DEFAULT_KEY,
        stringRes(R.string.default_word)
    ) { o1, o2 ->
        o1.order - o2.order
    }

    val sortByLabel: SortBy<LocalEpisode> = SortBy<LocalEpisode>(
        "label",
        stringRes(R.string.name_word)
    ) { o1, o2 ->
        o1.label.compareTo(o2.label)
    }

    val sortList = listOf(sortByDefault, sortByLabel)


    private val exoPlayer: EasyExoPlayer by Injekt.injectLazy()
    private val settingPreferences: SettingPreferences by Injekt.injectLazy()
    private val localCartoonCase: LocalCartoonCase by Injekt.injectLazy()

    @Volatile
    private var isPlay = false
        set(value) {
            field = value
            isPlay.logi(TAG)
        }

    data class LocalPlayState(
        val isLoading: Boolean = false,
        val localCartoon: LocalCartoon? = null,
        val selectPlayLine: Int = 0,
        val curPlayingLine: LocalPlayLineWrapper? = null,
        val curPlayingEpisode: LocalEpisode? = null,
        val currentSortKey: String = SORT_DEFAULT_KEY,
        val isReverse: Boolean = false,
        val sorted: List<LocalPlayLineWrapper> = emptyList(),

        val deleteModePlayLine: LocalPlayLineWrapper? = null,
        val deleteSelection: Set<LocalEpisode> = hashSetOf(),
    )

    private val _flow = MutableStateFlow(LocalPlayState(isLoading = true))
    val flow = _flow.asStateFlow()

    private val _dialogFlow = MutableStateFlow<DialogState?>(null)
    val dialogFlow = _dialogFlow.asStateFlow()

    val sortState = SortState<LocalEpisode>(
        sortList,
        flow.map { it.currentSortKey }
            .stateIn(viewModelScope, SharingStarted.Lazily, DetailedViewModel.SORT_DEFAULT_KEY),
        flow.map { it.isReverse }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    )

    val playingTitle = mutableStateOf("")
    val isReversal = mutableStateOf(false)

    sealed class DialogState {

        data class Delete(val episodes: List<LocalEpisode>) : DialogState()
    }


    init {

        // 排序
        viewModelScope.launch {
            combine(
                _flow.map { it.currentSortKey }.distinctUntilChanged().stateIn(viewModelScope),
                _flow.map { it.isReverse }.distinctUntilChanged().stateIn(viewModelScope),
                localCartoonCase.flowLocalCartoon(uuid).distinctUntilChanged()
                    .stateIn(viewModelScope),
            ) { sortKey, isReverse, cartoon ->
                if(cartoon == null){
                    _flow.update {
                        it.copy(
                            localCartoon = null,
                            curPlayingEpisode = null,
                            sorted = emptyList()
                        )
                    }
                    return@combine
                }
                val sort = sortList.find { it.id == sortKey } ?: sortByDefault
                _flow.update { local ->
                    val list = cartoon.playLines.map {
                        LocalPlayLineWrapper(
                            it,
                            isReverse,
                            { true },
                            sort.comparator
                        )
                    }
                    val index = if (local.selectPlayLine >= 0 && local.selectPlayLine < list.size) {
                        local.selectPlayLine
                    } else {
                        0
                    }
                    val curPlayLine = list.getOrNull(index) ?: list.getOrNull(0)
                    val curPlayingEpisode = if (local.curPlayingEpisode != null && list.find {
                            it.sortedEpisodeList.indexOf(local.curPlayingEpisode) != -1
                        } != null) local.curPlayingEpisode else
                        curPlayLine?.sortedEpisodeList?.getOrNull(0)
                    local.copy(
                        isLoading = false,
                        sorted = list,
                        curPlayingLine = list.getOrNull(index) ?: list.getOrNull(0),
                        selectPlayLine = index,
                        localCartoon = cartoon,
                        curPlayingEpisode = curPlayingEpisode,
                        deleteModePlayLine = null,
                        deleteSelection = emptySet(),
                    )
                }
            }.collect()
        }

//        // 排序
//        viewModelScope.launch {
//            runCatching {
//                initJob.join()
//            }.onFailure {
//                it.printStackTrace()
//            }
//            combine(
//                flow.map { it.localCartoon?.playLines }.filterIsInstance<ArrayList<LocalPlayLine>>()
//                    .distinctUntilChanged().stateIn(viewModelScope),
//                flow.map { it.currentSortKey }.distinctUntilChanged().stateIn(viewModelScope),
//                flow.map { it.isReverse }.distinctUntilChanged().stateIn(viewModelScope),
//            ) { playLines, sortKey, isReverse ->
//                val sort = sortList.find { it.id == sortKey } ?: sortByDefault
//                val playLineWrappers = playLines.map {
//                    LocalPlayLineWrapper(
//                        it,
//                        isReverse,
//                        { true },
//                        sort.comparator
//                    )
//                }
//                _flow.update {
//                    it.copy(
//                        sorted = playLineWrappers
//                    )
//                }
//            }.collect()
//        }
    }

    fun onPlayLineSelect(index: Int) {
        _flow.update {
            it.copy(
                selectPlayLine = index
            )
        }
    }

    fun onEpisodeClick(playLine: LocalPlayLineWrapper, episode: LocalEpisode) {
        _flow.update {
            it.copy(
                curPlayingLine = playLine,
                curPlayingEpisode = episode
            )
        }
    }

    fun exitDeleteMode() {
        _flow.update {
            it.copy(
                deleteModePlayLine = null
            )
        }
    }

    fun deleteMode(playLine: LocalPlayLineWrapper) {
        _flow.update {
            if (it.deleteModePlayLine == playLine) {
                it.copy(
                    deleteModePlayLine = null,
                )
            } else {
                it.copy(
                    deleteModePlayLine = playLine,
                    deleteSelection = it.deleteSelection.toMutableSet().apply {
                        clear()
                    }
                )
            }
        }
    }

    fun onDeleteClick(episode: LocalEpisode) {
        _flow.update {
            it.copy(
                deleteSelection = it.deleteSelection.toMutableSet().apply {
                    if (contains(episode)) {
                        remove(episode)
                    } else {
                        add(episode)
                    }
                }
            )
        }
        if (_flow.value.deleteSelection.isEmpty()) {
            _flow.update {
                it.copy(
                    deleteModePlayLine = null
                )
            }
        }
    }

    fun onDeleteSelectionRevert() {
        _flow.update {
            it.copy(
                deleteSelection = it.deleteSelection.toMutableSet().apply {
                    it.curPlayingLine?.sortedEpisodeList?.forEach { episode ->
                        if (contains(episode)) {
                            remove(episode)
                        } else {
                            add(episode)
                        }
                    }
                }
            )
        }
    }

    fun onDeleteDialog() {
        viewModelScope.launch {
            val cur = flow.first()
            _dialogFlow.update {
                DialogState.Delete(cur.deleteSelection.toList())
            }
            exitDeleteMode()
        }
    }

    fun onFinalDelete(list: List<LocalEpisode>) {
        viewModelScope.launch {
            localCartoonCase.remove(list)
            clearPlay()
        }

    }

    fun tryNext() {
        if (!isPlay) return
        viewModelScope.launch {
            if (!isPlay) return@launch
            val cur = _flow.first()
            cur.curPlayingEpisode ?: return@launch
            val old = cur.curPlayingLine?.sortedEpisodeList?.indexOf(cur.curPlayingEpisode) ?: -1
            if (old == -1) {
                return@launch
            }
            val new = cur.curPlayingLine?.sortedEpisodeList?.getOrNull(old + 1) ?: return@launch
            stringRes(com.heyanle.easy_i18n.R.string.try_play_next).toast()
            play(new)
        }
    }

    fun onSortChange(key: String, isReverse: Boolean) {
        val sort = sortList.find { it.id == key } ?: sortByDefault
        _flow.update {
            it.copy(
                currentSortKey = sort.id,
                isReverse = isReverse
            )
        }

    }

    fun externalPlay(episode: LocalEpisode) {
        runCatching {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    APP,
                    "com.heyanle.easybangumi4.fileProvider",
                    File(episode.path)
                )
            } else {
                Uri.fromFile(File(episode.path))
            }
            APP.startActivity(Intent("android.intent.action.VIEW").apply {
                APP.grantUriPermission(
                    APP.getPackageName(),
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // for mx player https://mx.j2inter.com/api
                putExtra("video_list", arrayOf(uri))
            })
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun onDismissRequest() {
        _dialogFlow.update {
            null
        }
    }

    fun clearPlay() {
        isPlay = false
        exoPlayer.removeListener(this)
        exoPlayer.stop(TAG)

    }

    fun play(episode: LocalEpisode) {
        isPlay = false
        exoPlayer.pause()
        if (settingPreferences.useExternalVideoPlayer.get()) {
            externalPlay(episode)
            return
        }
        if (exoPlayer.scene != TAG) {
            exoPlayer.clearMediaItems()
        }
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(episode.path))))
        playingTitle.value = episode.label
        exoPlayer.prepare(TAG)
        exoPlayer.playWhenReady = true
        exoPlayer.removeListener(this)
        exoPlayer.addListener(this)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        "onPlaybackStateChanged ${playbackState}".logi(TAG)
        if(playbackState == Player.STATE_READY){
            isPlay = true
        }else if(playbackState == Player.STATE_ENDED) {
            if(isPlay && exoPlayer.scene == TAG){
                tryNext()
            }
        }
    }
}

class LocalPlayViewModelFactory(
    private val uuid: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocalPlayViewModel::class.java))
            return LocalPlayViewModel(uuid = uuid) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}