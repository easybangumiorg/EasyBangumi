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
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.download.entity.LocalCartoon
import com.heyanle.easybangumi4.download.entity.LocalEpisode
import com.heyanle.easybangumi4.download.entity.LocalPlayLine
import com.heyanle.easybangumi4.getter.LocalCartoonGetter
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2023/9/25.
 */
class LocalPlayViewModel(
    private val uuid: String,
) : ViewModel() {

    private val exoPlayer: ExoPlayer by Injekt.injectLazy()
    private val settingPreferences: SettingPreferences by Injekt.injectLazy()
    private val localCartoonGetter: LocalCartoonGetter by Injekt.injectLazy()

    data class LocalPlayState(
        val isLoading: Boolean = false,
        val localCartoon: LocalCartoon? = null,
        val selectPlayLine: Int = 0,
        val curPlayingLine: LocalPlayLine? = null,
        val curPlayingEpisode: LocalEpisode? = null,
    )

    private val _flow = MutableStateFlow(LocalPlayState(isLoading = true))
    val flow = _flow.asStateFlow()

    val playingTitle = mutableStateOf("")
    val isReversal = mutableStateOf(false)

    init {
        exoPlayer.stop()
        viewModelScope.launch {
            flow.map { it.curPlayingEpisode }.distinctUntilChanged().collectLatest {
                it?.let {
                    innerPlay(it)
                }
            }
        }

        viewModelScope.launch {
            val local = localCartoonGetter.findWithUUID(uuid = uuid)
            if (local == null) {
                _flow.update {
                    it.copy(
                        isLoading = false,
                        localCartoon = null,
                        selectPlayLine = 0,
                        curPlayingLine = null,
                        curPlayingEpisode = null,
                    )
                }
                return@launch
            }
            val playLineList = local.playLines
            if (playLineList.isEmpty()) {
                _flow.update {
                    it.copy(
                        isLoading = false,
                        localCartoon = local,
                        selectPlayLine = 0,
                        curPlayingLine = null,
                        curPlayingEpisode = null,
                    )
                }
                return@launch
            }
            val playLine = playLineList[0]
            if (playLine.list.isEmpty()) {
                _flow.update {
                    it.copy(
                        isLoading = false,
                        localCartoon = local,
                        selectPlayLine = 0,
                        curPlayingLine = playLine,
                        curPlayingEpisode = null,
                    )
                }
                return@launch
            } else {
                _flow.update {
                    it.copy(
                        isLoading = false,
                        localCartoon = local,
                        selectPlayLine = 0,
                        curPlayingLine = playLine,
                        curPlayingEpisode = playLine.list[0],
                    )
                }
                return@launch
            }
        }


    }

    fun onPlayLineSelect(index: Int) {
        _flow.update {
            it.copy(
                selectPlayLine = index
            )
        }
    }

    fun onEpisodeClick(playLine: LocalPlayLine, episode: LocalEpisode) {
        _flow.update {
            it.copy(
                curPlayingLine = playLine,
                curPlayingEpisode = episode
            )
        }
    }

    fun tryNext(){}


    fun externalPlay(episode: LocalEpisode){
        runCatching {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(APP, "com.heyanle.easybangumi4.fileProvider", File(episode.path))
            }else{
                Uri.fromFile(File(episode.path))
            }
            APP.startActivity(Intent("android.intent.action.VIEW").apply {
                APP.grantUriPermission(APP.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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

    private fun innerPlay(episode: LocalEpisode) {
        if (settingPreferences.useExternalVideoPlayer.get()) {
            externalPlay(episode)
            return
        }

        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(episode.path))))
        playingTitle.value = episode.label
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
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