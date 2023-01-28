package com.heyanle.easybangumi.ui.common.easy_player

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.drawToBitmap
import androidx.media.AudioAttributesCompat
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.video.VideoSize
import com.heyanle.eplayer_core.EasyPlayerManager
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.constant.EasyPlayerStatus
import com.heyanle.eplayer_core.constant.OtherPlayerEvent
import com.heyanle.eplayer_core.controller.IController
import com.heyanle.eplayer_core.controller.IControllerGetter
import com.heyanle.eplayer_core.player.IPlayer
import com.heyanle.eplayer_core.render.SurfaceViewRender
import com.heyanle.eplayer_core.utils.ActivityScreenHelper
import com.heyanle.eplayer_core.utils.AudioFocusHelper
import com.heyanle.eplayer_core.utils.MediaHelper
import com.heyanle.eplayer_core.utils.PlayUtils
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Created by HeYanLe on 2023/1/14 22:10.
 * https://github.com/heyanLE
 */
class BaseEasyPlayerView :
    FrameLayout,
    IPlayer,
    AudioFocusHelper.OnAudioFocusListener,
    View.OnKeyListener {
    private var innerPlayer: Player? = null
    val surfaceView: SurfaceViewRender = SurfaceViewRender(context)

    private val realViewContainer = FrameLayout(context)

    private val renderContainer = FrameLayout(context)

    // controller 容器和锁
    protected val controllers: LinkedHashSet<IController> = LinkedHashSet()
    protected val controllerLock = ReentrantReadWriteLock()

    // 当前播放状态和播放器状态
    private var mCurrentPlayerState = EasyPlayerStatus.PLAYER_NORMAL
    private var mCurrentPlayState = EasyPlayStatus.STATE_IDLE

    // 当前进度（临时变量）
    private var mCurrentPosition: Long = 0L

    // 当前缩放情况
    private var mCurrentScaleType: Int = EasyPlayerManager.screenScaleType

    // 音量焦点
    protected var audioFocusHelper: AudioFocusHelper = AudioFocusHelper(context)
    var enableAudioFocus = EasyPlayerManager.enableAudioFocus

    protected var mVideoSize = intArrayOf(0, 0)

    private var mIsFullScreen = false

    init {
        realViewContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        addView(
            realViewContainer, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        realViewContainer.addView(
            renderContainer,
            0,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        realViewContainer.setBackgroundColor(Color.BLACK)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        lp.gravity = Gravity.CENTER
        renderContainer.addView(surfaceView, lp)
        audioFocusHelper.setListener(this)

    }

    // == Controller 管理和 事件分发 ============

    fun addController(isAddToParent: Boolean = true, vararg controller: IController) {
        controllerLock.write {
            for (c in controller) {
                controllers.add(c)
                c.attachToPlayer(this)
                if (isAddToParent) {
                    realViewContainer.addView(c.getViewContainer())
                }
            }
        }
    }

    fun removeController(vararg controller: IController) {
        controllerLock.write {
            for (c in controller) {
                controllers.remove(c)
                c.detachPlayer(this)
                realViewContainer.removeView(c.getViewContainer())
            }
        }
    }

    private fun getControllersSnapshot(): LinkedHashSet<IController> {
        val res = LinkedHashSet<IController>()
        controllerLock.read {
            res.addAll(controllers)
        }
        return res
    }

    protected fun dispatchPlayerStateChange(playerState: Int) {
        mCurrentPlayerState = playerState
        runWithController {
            dispatchPlayerStateChange(playerState)
        }
    }

    fun dispatchPlayStateChange(playState: Int) {
        if (innerPlayer?.playerError != null && playState != EasyPlayStatus.STATE_ERROR) {
            dispatchPlayStateChange(
                EasyPlayStatus.STATE_ERROR
            )
        }
        renderContainer.keepScreenOn = playState == EasyPlayStatus.STATE_PLAYING
        mCurrentPlayState = playState

        runWithController {
            dispatchPlayStateChange(playState)
        }
    }

    protected inline fun runWithController(block: IController.() -> Unit) {
        controllerLock.read {
            controllers.iterator().forEach {
                it.block()
            }
        }
    }

    override fun startFullScreen(changeScreen: Boolean) {
        if (mIsFullScreen) {
            return
        }
        runWithEnvironmentIfNotNull {
            val activity = getActivity() ?: return
            val decorView = (activity.window.decorView as? ViewGroup) ?: return
            mIsFullScreen = true

            if (changeScreen) {
                // 横屏
                ActivityScreenHelper.activityScreenOrientationLandscape(activity)
            }

            // 移除视图
            removeView(realViewContainer)

            // 添加 decorView
            decorView.addView(realViewContainer)

            // 分发事件
            dispatchPlayerStateChange(EasyPlayerStatus.PLAYER_FULL_SCREEN)

            // 隐藏状态栏和虚拟按键
            MediaHelper.setIsSystemBarsShow(activity, false)
            MediaHelper.setSystemBarsBehavior(
                activity,
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            )

            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener(this@BaseEasyPlayerView)
        }
    }

    override fun stopFullScreen(changeScreen: Boolean) {
        if (!mIsFullScreen) return
        val activity = getActivity() ?: return
        val decorView = (activity.window.decorView as? ViewGroup) ?: return
        mIsFullScreen = false
        if (changeScreen) {
            // 竖屏
            ActivityScreenHelper.activityScreenOrientationPortrait(activity)
        }

        // 从  decorView 中移除
        decorView.removeView(realViewContainer)

        // 添加到自身
        addView(realViewContainer)

        // 分发事件
        dispatchPlayerStateChange(EasyPlayerStatus.PLAYER_NORMAL)

        // 展示状态栏和虚拟按钮
        MediaHelper.setIsSystemBarsShow(activity, true)

    }

    override fun isFullScreen(): Boolean {
        return mIsFullScreen
    }

    // IPlayer
    fun onVideoSizeChanged(videoSize: VideoSize) {
        mVideoSize[0] = videoSize.width
        mVideoSize[1] = videoSize.height
        surfaceView.setVideoSize(videoSize.width, videoSize.height)

    }

    override fun doScreenShot(): Bitmap? {
        return surfaceView?.drawToBitmap()
    }

    override fun getBufferedPercentage(): Int {
        return innerPlayer?.bufferedPercentage ?: -1
    }

    override fun getCurrentPosition(): Long {
        return innerPlayer?.currentPosition ?: -1
    }

    override fun getDuration(): Long {
        return innerPlayer?.duration ?: -1
    }

    override fun getSpeed(): Float {
        return innerPlayer?.playbackParameters?.speed ?: 1.0F
    }

    override fun getVideoSize(): IntArray {
        return mVideoSize
    }

    override fun isMute(): Boolean {
        return false
    }

    override fun isPlaying(): Boolean {
        runWithEnvironmentIfNotNull {
            return when (this.playbackState) {
                Player.STATE_BUFFERING, Player.STATE_READY -> this.playWhenReady
                Player.STATE_IDLE, Player.STATE_ENDED -> false
                else -> false
            }
        }
        return false
    }

    override fun isTinyScreen(): Boolean {
        return false
    }

    override fun pause() {
        if (isInPlaybackState()) {
            innerPlayer?.playWhenReady = false
        }
    }

    override fun replay(resetPosition: Boolean) {
        if (resetPosition) {
            runWithEnvironmentIfNotNull {
                stop()
                clearMediaItems()
            }
        }
        innerPlayer?.stop()
        innerPlayer?.prepare()
    }

    override fun seekTo(pos: Long) {
        if (isInPlaybackState()) {
            runWithEnvironmentIfNotNull {
                seekTo(pos)
            }
        }
    }

    override fun setMirrorRotation(enable: Boolean) {
        surfaceView?.scaleX = if (enable) -1f else 1f
    }

    override fun setMute(isMute: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun setPlayerRotation(rotation: Float) {
        surfaceView?.setVideoRotation(rotation.toInt())
    }

    override fun setScreenScaleType(screenScaleType: Int) {
        mCurrentScaleType = screenScaleType
        surfaceView?.setScaleType(screenScaleType)
    }

    override fun setSpeed(speed: Float) {
        runWithEnvironmentIfNotNull {
            playbackParameters = playbackParameters.withSpeed(speed)
        }
    }

    override fun start() {
        innerPlayer?.play()
    }

    override fun startTinyScreen(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun stopTinyScreen() {
        throw UnsupportedOperationException()
    }

    private var mStartRequested: Boolean = false
    private var mPausedForLoss = false
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                if (mStartRequested || mPausedForLoss) {
                    start()
                    mStartRequested = false
                    mPausedForLoss = false
                }
                runWithEnvironmentIfNotNull {
                    volume = 1.0f
                } //恢复音量


            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (isPlaying()) {
                    mPausedForLoss = true
                    pause()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                runWithEnvironmentIfNotNull {
                    if (isPlaying) {
                        volume = 0.1f
                    }
                }

            }
        }
    }

    // == Player.Listener ==============

//    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
//        super.onPlayWhenReadyChanged(playWhenReady, reason)
//        if(innerPlayer?.playbackState == STATE_READY ){
//            if(playWhenReady){
//                dispatchPlayStateChange(EasyPlayStatus.STATE_PLAYING)
//            }else{
//                dispatchPlayStateChange(EasyPlayStatus.STATE_PAUSED)
//            }
//        }
//    }
//
//    override fun onPlaybackStateChanged(playbackState: Int) {
//        when(playbackState){
//            STATE_IDLE -> {
//                dispatchPlayStateChange(EasyPlayStatus.STATE_IDLE)
//            }
//            STATE_BUFFERING -> {
//                dispatchPlayStateChange(EasyPlayStatus.STATE_BUFFERING)
//            }
//            STATE_READY -> {
//                dispatchPlayStateChange(EasyPlayStatus.STATE_BUFFERED)
//                if(innerPlayer?.playWhenReady == true){
//                    dispatchPlayStateChange(EasyPlayStatus.STATE_PLAYING)
//                }else{
//                    dispatchPlayStateChange(EasyPlayStatus.STATE_PAUSED)
//                }
//
//            }
//            STATE_ENDED -> {
//                dispatchPlayStateChange(EasyPlayStatus.STATE_PLAYBACK_COMPLETED)
//            }
//        }
//    }
//
//    override fun onPlayerErrorChanged(error: PlaybackException?) {
//        super.onPlayerErrorChanged(error)
//        Log.d("BaseEasyPlayerView", error.toString())
//        if(error != null){
//            dispatchPlayStateChange(EasyPlayStatus.STATE_ERROR)
//        }
//    }
//
//    override fun onPlayerError(error: PlaybackException) {
//        super.onPlayerError(error)
//        Log.d("BaseEasyPlayerView", error.toString())
//        dispatchPlayStateChange(EasyPlayStatus.STATE_ERROR)
//    }
//
//    override fun onIsPlayingChanged(isPlaying: Boolean) {
//        super.onIsPlayingChanged(isPlaying)
//        if(isPlaying){
//            renderContainer.keepScreenOn = true
//            dispatchPlayStateChange(EasyPlayStatus.STATE_PLAYING)
//        }else{
//            renderContainer.keepScreenOn = false
//        }
//    }
//
//    override fun onEvents(player: Player, events: Player.Events) {
//        super.onEvents(player, events)
//
//    }
//
//    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
//        super.onAvailableCommandsChanged(availableCommands)
//        if(availableCommands.contains(Player.COMMAND_PREPARE)){
//            dispatchPlayStateChange(EasyPlayStatus.STATE_PREPARING)
//        }
//    }
//
//    override fun onIsLoadingChanged(isLoading: Boolean) {
//        super.onIsLoadingChanged(isLoading)
//        if(isLoading){
//            // dispatchPlayStateChange(EasyPlayStatus.STATE_PREPARING)
//        }
//    }


    // == PlayerView 自带相关逻辑 ===============

    fun release() {
        if (!isInIdleState()) {

            //关闭AudioFocus监听
            audioFocusHelper.abandonFocus()
            //关闭屏幕常亮
            renderContainer.keepScreenOn = false

            //重置播放进度
            mCurrentPosition = 0
            //切换转态
            dispatchPlayStateChange(EasyPlayStatus.STATE_IDLE)
        }
    }

    protected open fun isInPlaybackState(): Boolean {
        return mCurrentPlayState != EasyPlayStatus.STATE_ERROR &&
                mCurrentPlayState != EasyPlayStatus.STATE_IDLE &&
                mCurrentPlayState != EasyPlayStatus.STATE_PREPARING &&
                mCurrentPlayState != EasyPlayStatus.STATE_START_ABORT &&
                mCurrentPlayState != EasyPlayStatus.STATE_PLAYBACK_COMPLETED
    }

    protected open fun isInIdleState(): Boolean {
        return mCurrentPlayState == EasyPlayStatus.STATE_IDLE
    }

    protected open fun isInStartAbortState(): Boolean {
        return mCurrentPlayState == EasyPlayStatus.STATE_START_ABORT
    }

    fun attachToPlayer(player: Player) {
        innerPlayer = player
        player.setVideoSurfaceView(surfaceView)
    }

    fun detachToPlayer() {
        innerPlayer = null
    }

    fun refreshStateOnce() {


    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            return onBackPress()
        }
        return false
    }

    private fun findEnvironmentAndControllerFromChildren() {
        val viewList = ArrayList<View>()
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            Log.d("BaseEasyPlayer", "v -> ${v.toString()}")
            viewList.add(v)
        }
        viewList.forEach { v ->

            when (v) {

                is IControllerGetter -> {
                    removeView(v)
                    addController(true, v.getController())
                }
            }
        }
    }

    fun onBackPress(): Boolean {
        if (mIsFullScreen) {
            stopFullScreen()
            return true
        }
        return false
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findEnvironmentAndControllerFromChildren()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (mIsFullScreen) {
            isFocusableInTouchMode = true
            requestFocus()
            if (hasWindowFocus) {
                MediaHelper.setSystemBarsBehavior(
                    requireActivity(),
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                )
            }
        }
    }

    private fun requireActivity(): Activity {
        return getActivity() ?: throw IllegalStateException("BaseEasyPlayer Not in Activity")
    }

    private fun getActivity(): Activity? {
        return PlayUtils.findActivity(context)
    }

    private fun requestFocusIfNeed() {
        if (!isMute() && enableAudioFocus) {
            audioFocusHelper.requestFocusCompat(
                AudioAttributesCompat.USAGE_MEDIA,
                AudioAttributesCompat.CONTENT_TYPE_MOVIE
            )
        }
    }

    private inline fun runWithEnvironmentIfNotNull(block: Player.() -> Unit) {
        innerPlayer?.let {
            it.block()
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)
}