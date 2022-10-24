package com.heyanle.easy_player.controller

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.heyanle.easy_player.constant.EasyPlayStatus
import com.heyanle.easy_player.constant.EasyPlayerStatus
import com.heyanle.easy_player.utils.PlayUtils
import kotlin.concurrent.read
import kotlin.math.abs

/**
 * Create by heyanlin on 2022/10/24
 */
abstract class GestureVideoController: BaseController,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    OnTouchListener {

    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mAudioManager: AudioManager

    private var mStreamVolume = 0
    private var mBrightness = 0f
    private var mSeekPosition = -1L
    private var mFirstTouch = false
    private var mChangePosition = false
    private var mChangeBrightness = false
    private var mChangeVolume = false
    private var mCanSlide = false
    private var mCurPlayState = 0

    // 整个视频区域从最左划到最右滑过的视频时间
    var slideFullTime = 120000
    var isGestureEnabled = true
    var canChangePosition = true
    var enableInNormal = false
    var isDoubleTapTogglePlayEnabled = true

    override fun init() {
        super.init()
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mGestureDetector = GestureDetector(context, this)
        setOnTouchListener(this)
    }

    override fun setPlayerState(playerState: Int) {
        super.setPlayerState(playerState)
        if (playerState == EasyPlayerStatus.PLAYER_NORMAL) {
            mCanSlide = enableInNormal
        } else if (playerState == EasyPlayerStatus.PLAYER_FULL_SCREEN) {
            mCanSlide = true
        }
    }

    override fun setPlayState(playState: Int) {
        super.setPlayState(playState)
        mCurPlayState = playState
    }
    private fun isInPlaybackState(): Boolean {
        return controllerWrapper != null
                && mCurPlayState != EasyPlayStatus.STATE_ERROR
                && mCurPlayState != EasyPlayStatus.STATE_IDLE
                && mCurPlayState != EasyPlayStatus.STATE_PREPARING
                && mCurPlayState != EasyPlayStatus.STATE_PREPARED
                && mCurPlayState != EasyPlayStatus.STATE_START_ABORT
                && mCurPlayState != EasyPlayStatus.STATE_PLAYBACK_COMPLETED
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }

    /**
     * 手指按下的瞬间
     */
    override fun onDown(e: MotionEvent): Boolean {
        if (!isInPlaybackState() //不处于播放状态
            || !isGestureEnabled //关闭了手势
            || isEdge(e)
        ) //处于屏幕边沿
            return true
        mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val activity: Activity = PlayUtils.findActivity(context) ?: return false
        mBrightness = activity.window?.attributes?.screenBrightness ?: 0f
        mFirstTouch = true
        mChangePosition = false
        mChangeBrightness = false
        mChangeVolume = false
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        if (isInPlaybackState()) {
            controllerWrapper?.toggleShowState()
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        if (isDoubleTapTogglePlayEnabled && !isLocked() && isInPlaybackState()) togglePlay()
        return true
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!isInPlaybackState() //不处于播放状态
            || !isGestureEnabled //关闭了手势
            || !mCanSlide //关闭了滑动手势
            || isLocked() //锁住了屏幕
            || isEdge(e1)
        ) //处于屏幕边沿
            return true
        val deltaX = e1.x - e2.x
        val deltaY = e1.y - e2.y
        if (mFirstTouch) {
            mChangePosition = abs(distanceX) >= abs(distanceY)
            if (!mChangePosition) {
                //半屏宽度
                if (e2.x > width/2f) {
                    mChangeVolume = true
                } else {
                    mChangeBrightness = true
                }
            }

            if (mChangePosition) {
                //根据用户设置是否可以滑动调节进度来决定最终是否可以滑动调节进度
                mChangePosition = canChangePosition
            }
            if (mChangePosition || mChangeBrightness || mChangeVolume) {
                componentsLock.read {
                    components.iterator().forEach {
                        (it.key as? IGestureComponent)?.onStartSlide()
                    }
                }
            }
            mFirstTouch = false

        }
        if (mChangePosition) {
            slideToChangePosition(deltaX)
        } else if (mChangeBrightness) {
            slideToChangeBrightness(deltaY)
        } else if (mChangeVolume) {
            slideToChangeVolume(deltaY)
        }
        return true
    }

    protected open fun slideToChangePosition(dx: Float) {
        var deltaX = dx
        deltaX = -deltaX
        val width = measuredWidth
        val duration = controllerWrapper?.getDuration()?:0L
        val currentPosition = controllerWrapper?.getCurrentPosition()?:0L
        var position = (deltaX / width * 120000 + currentPosition).toLong()
        if (position > duration) position = duration
        if (position < 0) position = 0

        componentsLock.read {
            components.iterator().forEach {
                (it.key as? IGestureComponent)?.onPositionChange(position, currentPosition, duration)
            }
        }
        mSeekPosition = position
    }

    protected open fun slideToChangeBrightness(deltaY: Float) {
        val activity: Activity = PlayUtils.findActivity(context) ?: return
        val window = activity.window
        val attributes = window.attributes
        val height = measuredHeight
        if (mBrightness == -1.0f) mBrightness = 0.5f
        var brightness = deltaY * 2 / height + mBrightness
        if (brightness < 0) {
            brightness = 0f
        }
        if (brightness > 1.0f) brightness = 1.0f
        val percent = (brightness * 100).toInt()
        attributes.screenBrightness = brightness
        window.attributes = attributes

        componentsLock.read {
            components.iterator().forEach {
                (it.key as? IGestureComponent)?.onBrightnessChange(percent)
            }
        }
    }

    protected open fun slideToChangeVolume(deltaY: Float) {
        val streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val height = measuredHeight
        val deltaV = deltaY * 2 / height * streamMaxVolume
        var index = mStreamVolume + deltaV
        if (index > streamMaxVolume) index = streamMaxVolume.toFloat()
        if (index < 0) index = 0f
        val percent = (index / streamMaxVolume * 100).toInt()
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index.toInt(), 0)
        componentsLock.read {
            components.iterator().forEach {
                (it.key as? IGestureComponent)?.onVolumeChange(percent)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        //滑动结束时事件处理
        if (!mGestureDetector.onTouchEvent(event)) {
            val action = event!!.action
            when (action) {
                MotionEvent.ACTION_UP -> {
                    stopSlide()
                    if (mSeekPosition >= 0) {
                        controllerWrapper?.seekTo(mSeekPosition)
                        mSeekPosition = -1
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    stopSlide()
                    mSeekPosition = -1
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun stopSlide() {
        componentsLock.read {
            components.iterator().forEach {
                (it.key as? IGestureComponent)?.onStopSlide()
            }
        }
    }

    private fun isEdge(e: MotionEvent): Boolean{
        val edgeSize = PlayUtils.dp2px(context, 40f)
        return e.x < edgeSize || e.x > width - edgeSize
                ||e.y < edgeSize || e.y > height - edgeSize
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {}

    override fun onShowPress(e: MotionEvent?) {}

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }


    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
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