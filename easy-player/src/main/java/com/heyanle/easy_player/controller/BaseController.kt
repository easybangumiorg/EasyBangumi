package com.heyanle.easy_player.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.RelativeLayout
import com.heyanle.easy_player.constant.EasyPlayStatus
import com.heyanle.easy_player.constant.EasyPlayerStatus
import com.heyanle.easy_player.player.IEasyPlayer
import com.heyanle.easy_player.utils.OrientationHelper
import com.heyanle.easy_player.utils.PlayUtils
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 1.播放器状态改变
 * 2.播放状态改变
 * 3.控制视图的显示和隐藏
 * 4.播放进度改变
 * 5.锁定状态改变
 * 6.设备方向监听
 * Created by HeYanLe on 2022/10/23 13:05.
 * https://github.com/heyanLE
 */
abstract class BaseController:
    RelativeLayout,
    IComponentContainer,
    IEasyPlayer.EventListener ,
    OrientationHelper.OnOrientationChangeListener
{

    var fadeOutTimeout = 4000L

    protected var activity: WeakReference<Activity>? = null

    protected val easyPlayer: AtomicReference<IEasyPlayer?> = AtomicReference(null)

    protected val components: LinkedHashMap<IControlComponent, Boolean> = linkedMapOf()
    protected val componentsLock = ReentrantReadWriteLock()

    protected var controllerWrapper: ControllerWrapper? = null

    private var isShowing = false
    private var isLocked = false

    //是否开启根据屏幕方向进入/退出全屏
    protected var enableOrientation = false

    //屏幕方向监听辅助类
    protected val mOrientationHelper: OrientationHelper = OrientationHelper(context)

    //是否开始刷新进度
    protected var isStartProgress = false

    protected val mShowAnim: Animation = AlphaAnimation(0f, 1f).apply {
        duration = 300
    }
    protected val mHideAnim: Animation= AlphaAnimation(1f, 0f).apply {
        duration = 300
    }

    protected val hideRunnable = Runnable {
        hide()
    }

    init {
        init()
    }

    protected open fun init(){
        PlayUtils.findActivity(context)?.let {
            activity = WeakReference(it)
        }
        getLayoutId().let {
            if(it != 0){
                LayoutInflater.from(context).inflate(it, this, true)
            }
        }
    }

    protected val updateProgressRunnable = object: Runnable {
        override fun run() {
            controllerWrapper?.let {
                val position = it.getCurrentPosition()
                val duration = it.getDuration()
                handleProgressUpdate(duration, position)
                if(it.isPlaying()){
                    postDelayed(this, ((1000 - position % 1000) / it.getSpeed()).toLong())
                }else{
                    isStartProgress = false
                }
            }
        }
    }

    override fun startFadeOut() {
        removeCallbacks(hideRunnable)
        postDelayed(hideRunnable, fadeOutTimeout)
    }

    override fun getViewContainer(): ViewGroup {
        return this
    }

    override fun stopFadeOut() {
        removeCallbacks(hideRunnable)
    }

    override fun isShowing(): Boolean {
        return isShowing
    }

    override fun setLocked(locked: Boolean) {
        isLocked = locked
        handleLockStateChanged(locked)
    }

    override fun isLocked(): Boolean {
        return isLocked
    }

    override fun startProgressUpdate() {
        isStartProgress = true
        removeCallbacks(updateProgressRunnable)
        post(updateProgressRunnable)
    }

    override fun stopProgressUpdate() {
        isStartProgress = false
        removeCallbacks(updateProgressRunnable)
    }

    override fun hide() {
        if(isShowing){
            stopFadeOut()
            handleVisibilityChanged(false, mHideAnim)
            isShowing = false
        }
    }

    override fun show() {
        if(!isShowing){
            startFadeOut()
            handleVisibilityChanged(true, mHideAnim)
            isShowing = true
        }
    }

    override fun addComponents(isAddToViewGroup: Boolean, vararg component: IControlComponent) {
        componentsLock.write {
            for(c in component){
                removeComponents(c)
                components[c] = isAddToViewGroup
                controllerWrapper?.let {
                    c.onAttachToController(it)
                }
                if(isAddToViewGroup){
                    val params = c.getLayoutParam()
                    val view = c.getView()
                    if(view != null){
                        if(params == null){
                            addView(view)
                        }else{
                            addView(view, params)
                        }
                    }
                }
            }
        }

    }

    override fun removeComponents(vararg component: IControlComponent) {
        componentsLock.write {
            for(c in component){
                if(components.containsKey(c)){
                    removeView(c.getView())
                    components.remove(c)
                }
            }

        }
    }

    override fun removeAllComponentsWithoutAddToViewGroup() {
        componentsLock.write {
            val it = components.iterator()
            while(it.hasNext()){
                val c = it.next()
                if(!c.value){
                    it.remove()
                }
            }
        }
    }

    override fun removeAllComponents() {
        removeAllViews()
        componentsLock.write {
            components.clear()
        }

    }

    override fun setEasyPlayer(player: IEasyPlayer) {
        val old = easyPlayer.get()
        if(easyPlayer.compareAndSet(old, player)){
            old?.removeEventListener(this)
            player.setEventListener(this)
        }
    }

    override fun getEasyPlayer(): IEasyPlayer? {
        return easyPlayer.get()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (controllerWrapper?.isPlaying() == true
            && (enableOrientation || controllerWrapper?.isFullScreen() == true)
        ) {
            if (hasWindowFocus) {
                postDelayed({ mOrientationHelper.enable() }, 800)
            } else {
                mOrientationHelper.disable()
            }
        }
    }

    protected var mOrientation = 0
    override fun onOrientationChanged(orientation: Int) {
        val act = activity?.get()?:return

        val lastOrientation = mOrientation
        if(orientation == OrientationEventListener.ORIENTATION_UNKNOWN){
            mOrientation = -1
            return
        }

        if(orientation > 350 || orientation < 10){
            val o: Int = act.requestedOrientation
            if (o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && lastOrientation == 0) return
            if (mOrientation == 0) return
            //0度，用户竖直拿着手机
            mOrientation = 0
            onOrientationPortrait(act)
        } else if(orientation in 81..99) {
            val o: Int = act.requestedOrientation
            if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && lastOrientation == 90) return
            if (mOrientation == 90) return
            //90度，用户右侧横屏拿着手机
            mOrientation = 90
            onOrientationReverseLandscape(act)
        } else if (orientation in 261..279) {
            val o: Int = act.requestedOrientation
            //手动切换横竖屏
            if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && lastOrientation == 270) return
            if (mOrientation == 270) return
            //270度，用户左侧横屏拿着手机
            mOrientation = 270
            onOrientationLandscape(act)
        }
    }

    /**
     * 竖屏
     */
    @SuppressLint("SourceLockedOrientationActivity")
    protected open fun onOrientationPortrait(activity: Activity) {
        //屏幕锁定的情况
        if (isLocked) return
        //没有开启设备方向监听的情况
        if (!enableOrientation) return
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        controllerWrapper?.stopFullScreen()
    }

    /**
     * 横屏
     */
    protected open fun onOrientationLandscape(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (controllerWrapper?.isFullScreen() == true) {
            handlePlayerStateChanged(EasyPlayerStatus.PLAYER_FULL_SCREEN)
        } else {
            controllerWrapper?.startFullScreen()
        }
    }

    /**
     * 反向横屏
     */
    protected open fun onOrientationReverseLandscape(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        if (controllerWrapper?.isFullScreen() == true) {
            handlePlayerStateChanged(EasyPlayerStatus.PLAYER_FULL_SCREEN)
        } else {
            controllerWrapper?.startFullScreen()
        }
    }

    open fun togglePlay(){
        controllerWrapper?.togglePlay()
    }

    open fun toggleFullScreen(){
        val act = activity?.get()?:return
        controllerWrapper?.toggleFullScreen(act)
    }

    open fun startFullScreen(){
        val act = activity?.get()?:return
        if(act.isFinishing) return
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        controllerWrapper?.startFullScreen()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    open fun stopFullScreen(){
        val act = activity?.get()?:return
        if(act.isFinishing) return
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        controllerWrapper?.stopFullScreen()
    }

    open fun setPlayState(playState: Int) {
        handlePlayStateChanged(playState)
    }


    open fun setPlayerState(playerState: Int) {
        handlePlayerStateChanged(playerState)
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    abstract fun getLayoutId(): Int

    protected open fun onHandleVisibilityChanged(isVisible: Boolean, anim: Animation){}

    protected open fun onHandleLockStateChanged(isLocked: Boolean){}

    protected open fun onHandleProgressUpdate(duration: Long, position: Long) {}

    protected open fun onPlayerStateChanged(playerState: Int) {
        when (playerState) {
            EasyPlayerStatus.PLAYER_NORMAL -> {
                if(enableOrientation){
                    mOrientationHelper.enable()
                }else{
                    mOrientationHelper.disable()
                }
            }
            EasyPlayerStatus.PLAYER_FULL_SCREEN -> {
                mOrientationHelper.enable()
            }
            EasyPlayerStatus.PLAYER_TINY_SCREEN -> {
                mOrientationHelper.disable()
            }
//            VideoView.PLAYER_NORMAL -> {
//                if (mEnableOrientation) {
//                    mOrientationHelper.enable()
//                } else {
//                    mOrientationHelper.disable()
//                }
//                if (hasCutout()) {
//                    CutoutUtil.adaptCutoutAboveAndroidP(context, false)
//                }
//            }
//            VideoView.PLAYER_FULL_SCREEN -> {
//                //在全屏时强制监听设备方向
//                mOrientationHelper.enable()
//                if (hasCutout()) {
//                    CutoutUtil.adaptCutoutAboveAndroidP(context, true)
//                }
//            }
//            VideoView.PLAYER_TINY_SCREEN -> mOrientationHelper.disable()
        }
    }

    protected open fun onPlayStateChanged(playState: Int){
        when (playState) {
            EasyPlayStatus.STATE_IDLE -> {
                mOrientationHelper.disable()
                mOrientation = 0
                isLocked = false
                isShowing = false
                //由于游离组件是独立于控制器存在的，
                //所以在播放器release的时候需要移除
                removeAllComponentsWithoutAddToViewGroup()
            }
            EasyPlayStatus.STATE_PLAYBACK_COMPLETED -> {
                isLocked = false
                isShowing = false
            }
            EasyPlayStatus.STATE_ERROR -> isShowing = false
        }
    }

    //------------------------ start handle event change ------------------------//
    protected open fun handleVisibilityChanged(isVisible: Boolean, anim: Animation) {
        if (!isLocked) { //没锁住时才向ControlComponent下发此事件
            componentsLock.read {
                components.iterator().forEach {
                    it.key.onVisibleChanged(isVisible, anim)
                }
            }
        }
        onHandleVisibilityChanged(isVisible, anim)
    }

    protected open fun handleLockStateChanged(locked: Boolean) {
        componentsLock.read {
            components.iterator().forEach {
                it.key.onLockStateChange(locked)
            }
        }
        onHandleLockStateChanged(locked)
    }

    protected fun handleProgressUpdate(duration: Long, position: Long){
        componentsLock.read {
            components.iterator().forEach {
                it.key.onProgressUpdate(duration, position)
            }
        }
        onHandleProgressUpdate(duration, position)
    }



    protected open fun handlePlayerStateChanged(playerState: Int) {
        componentsLock.read {
            components.iterator().forEach {
                it.key.onPlayerStateChanged(playerState)
            }
        }
        onPlayerStateChanged(playerState)
    }

    protected open fun handlePlayStateChanged(playState: Int){
        componentsLock.read {
            components.iterator().forEach {
                it.key.onPlayStateChanged(playState)
            }
        }
        onPlayStateChanged(playState)
    }


    open fun attachToPlayerController(playerController: IPlayerController){
        val wrapper = ControllerWrapper(this, playerController)
        controllerWrapper = wrapper
        componentsLock.read {
            components.iterator().forEach {
                it.key.onAttachToController(wrapper)
            }
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