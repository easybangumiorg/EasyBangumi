package com.heyanle.easy_player.controller

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.RelativeLayout
import com.heyanle.easy_player.player.IEasyPlayer
import com.heyanle.easy_player.utils.OrientationHelper
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 1.播放器状态改变: {@link #handlePlayerStateChanged(int)}
 * 2.播放状态改变: {@link #handlePlayStateChanged(int)}
 * 3.控制视图的显示和隐藏: {@link #handleVisibilityChanged(boolean, Animation)}
 * 4.播放进度改变: {@link #handleSetProgress(int, int)}
 * 5.锁定状态改变: {@link #handleLockStateChanged(boolean)}
 * 6.设备方向监听: {@link #onOrientationChanged(int)}
 * Created by HeYanLe on 2022/10/23 13:05.
 * https://github.com/heyanLE
 */
class BaseController: RelativeLayout, IComponentContainer, IEasyPlayer.EventListener {

    private val easyPlayer: AtomicReference<IEasyPlayer?> = AtomicReference(null)

    private val components: LinkedHashMap<IControlComponent, Boolean> = linkedMapOf()
    private val componentsLock = ReentrantReadWriteLock()

    private var controllerWrapper: ControllerWrapper? = null

    private var isShowing = false
    private var isLocked = false
    private var fadeOutTimeout = 4000L

    //是否开启根据屏幕方向进入/退出全屏
    private var enableOrientation = false

    //屏幕方向监听辅助类
    private val mOrientationHelper: OrientationHelper = OrientationHelper(context)

    private var isAdaptCutout = false
    //是否有刘海
    private val mHasCutout: Boolean? = null

    //刘海的高度
    private var mCutoutHeight = 0

    //是否开始刷新进度
    private var isStartProgress = false

    private val mShowAnim: Animation = AlphaAnimation(0f, 1f).apply {
        duration = 300
    }
    private val mHideAnim: Animation= AlphaAnimation(1f, 0f).apply {
        duration = 300
    }

    private val showRunnable = Runnable {
        show()
    }

    private val hideRunnable = Runnable {
        hide()
    }

    private val updateProgressRunnable = object: Runnable {
        override fun run() {
            TODO("Not yet implemented")
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

    override fun onError() {
        TODO("Not yet implemented")
    }

    override fun onCompletion() {
        TODO("Not yet implemented")
    }

    override fun onPrepared() {
        TODO("Not yet implemented")
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun onRealPlayerEvent(event: Int, vararg args: Any) {
        TODO("Not yet implemented")
    }

    open fun onHandleControlEvent(event: ControlEvent){}

    //------------------------ start handle event change ------------------------//
    private fun handleVisibilityChanged(isVisible: Boolean, anim: Animation) {
        if (!isLocked) { //没锁住时才向ControlComponent下发此事件
            componentsLock.read {
                components.iterator().forEach {
                    it.key.handleEvent(ControlEvent.OnVisibleChanged(isVisible, anim))
                }
            }
        }
        onHandleControlEvent(ControlEvent.OnVisibleChanged(isVisible, anim))
    }


    fun attachToPlayerController(playerController: IPlayerController){
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