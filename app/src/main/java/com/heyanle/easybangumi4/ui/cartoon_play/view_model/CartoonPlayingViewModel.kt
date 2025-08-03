package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.CartoonRecordedModel
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.MediaAndroidUtils
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import loli.ball.easyplayer2.texture.TexturePlayerRender
import java.io.File

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
@UnstableApi
class CartoonPlayingViewModel(
) : ViewModel(), Player.Listener, TextureView.SurfaceTextureListener {

    companion object {
        const val TAG = "CartoonPlayingViewModel"
    }

    // 播放器状态 =================================================
    private val exoPlayerBuilder: ExoPlayer.Builder by Inject.injectLazy()
    val exoPlayer = exoPlayerBuilder.build().apply {
        addListener(this@CartoonPlayingViewModel)
    }

    // 渲染器 =================================================
    val easyTextRenderer: TexturePlayerRender = TexturePlayerRender()
        .apply {
            setExtSurfaceTextureListener(this@CartoonPlayingViewModel)
        }

    // 当前播放番剧缓存 =================================================
    private var cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState? = null
    private var playingPlayLine: PlayLine? = null
    private var playingEpisode: Episode? = null
    private var playingInfo: PlayerInfo? = null

    // 播放状态 =================================================
    data class PlayingState(
        val isLoading: Boolean = true,
        val isPlaying: Boolean = false,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val errorThrowable: Throwable? = null
    )

    private val _playingState = MutableStateFlow<PlayingState>(PlayingState())
    val playingState = _playingState.asStateFlow()

    // 协程
    private val dispatcher = CoroutineProvider.newSingleDispatcher
    private val singleScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val scope = MainScope()

    // 任务管理 =================================================
    // 加载任务
    private var lastJob: Job? = null

    // 获取缩略图任务
    private var thumbnailJob: Job? = null

    // 其他模块注入 =================================================
    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val cartoonMediaSourceFactory: CartoonMediaSourceFactory by Inject.injectLazy()
    private val sourceStateCase: SourceStateCase by Inject.injectLazy()
    private val settingPreferences: SettingPreferences by Inject.injectLazy()

    // 各种配置（找机会拆单独一个 ViewModel 和播放无关 =================================================
    private val customSpeedPref = settingPreferences.customSpeed
    val customSpeed = customSpeedPref.stateIn(viewModelScope)
    val isCustomSpeed = mutableStateOf(false)


    val videoScaleTypeSelection = settingPreferences.scaleTypeSelection
    private val videoScaleTypePref = settingPreferences.videoScaleType
    val videoScaleType = videoScaleTypePref.stateIn(viewModelScope)

    val isCustomSpeedDialog = mutableStateOf(false)

    val fastWeight = settingPreferences.fastWeight.stateIn(viewModelScope)
    val fastSecond = settingPreferences.fastSecond.stateIn(viewModelScope)

    val fastTopSecond = settingPreferences.fastTopSecond.stateIn(viewModelScope)

    val fastTopWeightMolecule = settingPreferences.fastWeightTopMolecule.stateIn(viewModelScope)
    val fastWeightTopDenominator = settingPreferences.fastWeightTopDenominator

    val playerSeekFullWidthTimeMS =
        settingPreferences.playerSeekFullWidthTimeMS.stateIn(viewModelScope)

    val defaultSpeed = settingPreferences.defaultSpeed.stateIn(viewModelScope)

    // 剪辑模式
    val showRecording = mutableStateOf<CartoonRecordedModel?>(null)

    // 缩略图缓存
    var thumbnailBuffer: ThumbnailBuffer? = null
    val thumbnailFolder: File = File(APP.getCachePath("thumbnail")).apply {
        deleteRecursively()
        mkdirs()
    }

    val imageCache: File = File(APP.getCachePath("image")).apply {
        deleteRecursively()
        mkdirs()
    }

    fun image() {
        val playerInfo = playingInfo
        if (playerInfo == null) {
            stringRes(com.heyanle.easy_i18n.R.string.waiting_parsing)
            return
        }
        val position = exoPlayer.currentPosition
        scope.launch(dispatcher) {
            val bmp = easyTextRenderer.getTextureViewOrNull()?.bitmap ?: return@launch
            val file = File(imageCache, "${position}.png")
            file.delete()
            file.createNewFile()
            bmp.compress(
                Bitmap.CompressFormat.PNG,
                100,
                file.outputStream()
            )
            MediaAndroidUtils.saveToDownload(
                file,
                type = "image",
                "image_${position}.png"
            )
            scope.launch {
                stringRes(com.heyanle.easy_i18n.R.string.image_save_completely).toast()
            }

        }
    }

    @OptIn(UnstableApi::class)
    fun showRecord() {
        val playerInfo = playingInfo
        if (playerInfo == null) {
            stringRes(com.heyanle.easy_i18n.R.string.waiting_parsing)
            return
        }
        showRecording.value = CartoonRecordedModel(
            APP,
            exoPlayer,
            playerInfo,
            cartoonMediaSourceFactory,
            scope,
            thumbnailBuffer ?: ThumbnailBuffer(thumbnailFolder),
            Math.max(0, exoPlayer.currentPosition - 30000),
            Math.min(exoPlayer.currentPosition + 30000, exoPlayer.duration),
            exoPlayer.currentPosition
        )
    }

    fun setCustomSpeedDialog() {
        isCustomSpeedDialog.value = true
    }

    fun setCustomSpeed(speed: Float) {
        customSpeedPref.set(speed)
        if (speed <= 0) {
            isCustomSpeed.value = false
        }
    }

    fun enableCustomSpeed() {
        if (customSpeed.value <= 0) {
            setCustomSpeedDialog()
        } else {
            isCustomSpeed.value = true
        }
    }

    fun disableCustomSpeed() {
        isCustomSpeed.value = false
    }

    fun setVideoScaleType(scaleType: Int) {
        videoScaleTypePref.set(scaleType)
    }

    // 刷新 & 播放 ===================================

    fun tryRefresh() {
        lastJob?.cancel()
        lastJob = scope.launch {
            cartoonPlayingState?.let {
                innerPlay(it, 0)
            }
        }

    }

    fun changePlay(
        cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState?,
        adviceProcess: Long,
    ) {
        lastJob?.cancel()
        lastJob = scope.launch {
            this@CartoonPlayingViewModel.cartoonPlayingState = cartoonPlayingState
            if (cartoonPlayingState == null) {
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = false,
                        isError = false
                    )
                }
            } else {
                if (playingPlayLine == cartoonPlayingState.playLine.playLine
                    && playingEpisode == cartoonPlayingState.episode
                    && _playingState.first().isPlaying
                    && exoPlayer.isMedia()
                ) {
                    if (adviceProcess >= 0) {
                        exoPlayer.seekTo(adviceProcess)
                    }
                } else {
                    innerPlay(cartoonPlayingState, adviceProcess)
                }
            }
        }

    }

    /**
     * 调用外部播放器播放，当前状态为 Playing 时候才有效
     * @return 当前播放状态是否为 Playing
     */
    fun playCurrentExternal(): Boolean {
        val oldPlayingState = _playingState.value
        if (oldPlayingState.isPlaying) {
            val playerInfo = playingInfo ?: return false
            innerPlayExternal(playerInfo)
            return true
        }
        return false
    }

    /**
     * 调用外部播放器播放
     */
    private fun innerPlayExternal(playerInfo: PlayerInfo) {
        var uri = playerInfo.uri.toUri()
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                try {
                    uri = FileProvider.getUriForFile(
                        APP,
                        APP.packageName + ".provider",
                        file
                    )
                }catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
        }
        APP.startActivity(Intent("android.intent.action.VIEW").apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    private suspend fun innerPlay(
        cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState,
        adviceProcess: Long,
    ) {

        exoPlayer.pause()
        _playingState.update {
            it.copy(
                isLoading = true,
            )
        }
        val play = sourceStateCase.awaitBundle().play(cartoonPlayingState.cartoonSummary.source)
        if (play == null) {
            _playingState.update {
                it.copy(
                    isLoading = false,
                    isError = true,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.source_not_found)
                )
            }
            return
        }
        play.getPlayInfo(
            cartoonPlayingState.cartoonSummary,
            cartoonPlayingState.playLine.playLine,
            cartoonPlayingState.episode
        )
            .complete {
                yield()
                it.data.uri.logi("CartoonPlayingViewModel")
                playingPlayLine = cartoonPlayingState.playLine.playLine
                playingEpisode = cartoonPlayingState.episode
                innerPlay(it.data, adviceProcess)
            }
            .error {
                yield()
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMsg = it.errorMsg,
                        errorThrowable = it.errorThrowable
                    )
                }
            }


    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private suspend fun innerPlay(playerInfo: PlayerInfo, adviceProcess: Long) {
        exoPlayer.pause()
        if (lastJob?.isCancelled != false || lastJob?.isActive != true) {
            return
        }
        if (this.playingInfo != null) {
            if (
                playingInfo?.uri == playerInfo.uri
                && playingInfo?.decodeType == playerInfo.decodeType
                && exoPlayer.isMedia()
            ) {
                playingInfo = playerInfo
                if (adviceProcess >= 0) {
                    exoPlayer.seekTo(adviceProcess)
                }
                exoPlayer.playWhenReady = true
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = true,
                        isError = false
                    )
                }
                return
            }
        }
        thumbnailBuffer?.clear()
        thumbnailFolder.deleteRecursively()
        thumbnailBuffer = ThumbnailBuffer(thumbnailFolder)
        playingInfo = playerInfo
        // 本地番源不过缓存
        val media =
            if (cartoonPlayingState?.cartoonSummary?.source?.equals(LocalSource.LOCAL_SOURCE_KEY) == true)
                cartoonMediaSourceFactory.getWithoutCache(playerInfo) else
                cartoonMediaSourceFactory.getWithCache(playerInfo)
        exoPlayer.setMediaSource(media, adviceProcess)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _playingState.update {
            it.copy(
                isLoading = false,
                isPlaying = true,
                isError = false
            )
        }
    }


    fun trySaveHistory(ps: Long = -1) {
        val line = playingPlayLine ?: return
        val epi = playingEpisode ?: return
        val cartoon = cartoonPlayingState?.cartoonSummary ?: return
        scope.launch {
            cartoonInfoDao.transaction {
                val old = cartoonInfoDao.getByCartoonSummary(cartoon.id, cartoon.source)
                if (old != null) {
                    val lineIndex = old.playLine.indexOf(line)
                    if (lineIndex >= 0) {
                        cartoonInfoDao.modify(
                            old.copyHistory(
                                lineIndex,
                                line,
                                epi,
                                if (ps >= 0) ps else exoPlayer.currentPosition
                            )
                        )
                    }

                }
            }
        }
    }

    // onDispose
    fun onExit() {
        if (_playingState.value.isPlaying && !exoPlayer.playWhenReady && exoPlayer.isMedia()) {
            trySaveHistory()
        }
        lastJob?.cancel()
        exoPlayer.pause()
    }

    // exoPlayer 回调 ==================================================

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (_playingState.value.isPlaying && !exoPlayer.playWhenReady && exoPlayer.isMedia()) {
            trySaveHistory()
        }

    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (_playingState.value.isPlaying && !exoPlayer.playWhenReady && exoPlayer.isMedia()) {
            trySaveHistory()
        }
    }

    // ViewModel clear

    override fun onCleared() {
        super.onCleared()
        lastJob?.cancel()
        scope.cancel()
        exoPlayer.release()
    }

    // surfaceTexture 回调 ==============================================

    private var lastThumbnailTime = 0L

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // 耗电有点猛，先关闭
//        //"onSurfaceTextureUpdated 1".logi(TAG)
//        if (thumbnailBuffer == null) {
//            return
//        }
//        scope.launch {
//            //"onSurfaceTextureUpdated 2".logi(TAG)
//            val currentPosition = exoPlayer.currentPosition
//            // 如果该进度前后两秒都没有缩略图就保存一张
//            val currentFile = thumbnailBuffer?.getThumbnail(currentPosition, 2000)
//            val current = System.currentTimeMillis()
//
//            // 频次控制
//            if (currentFile == null && current - lastThumbnailTime > 2000) {
//                lastThumbnailTime = current
//                //"onSurfaceTextureUpdated 3".logi(TAG)
//                // 保存缩略图
//                thumbnailJob?.cancel()
//                thumbnailJob = singleScope.launch {
//                    yield()
//                    val textureView = easyTextRenderer.getTextureViewOrNull() ?: return@launch
//                    val bmp = textureView.bitmap ?: return@launch
//                    thumbnailFolder.mkdirs()
//                    val file = File(thumbnailFolder, "${currentPosition}.jpg")
//                    file.delete()
//                    file.createNewFile()
//                    file.deleteOnExit()
//                    file.outputStream().use {
//                        bmp.compress(Bitmap.CompressFormat.JPEG, 10, it)
//                    }
//                    //"onSurfaceTextureUpdated 4".logi(TAG)
//                    thumbnailBuffer?.addThumbnail(currentPosition, file)
//                    bmp.recycle()
//                }
//
//            }
//        }
    }

    private fun ExoPlayer.isMedia(): Boolean {
        return playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY
    }
}