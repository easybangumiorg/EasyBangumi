package com.heyanle.easybangumi.ui.common.easy_player.component

import android.R.color
import android.animation.Animator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ComponentStandardBinding
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.ui.common.easy_player.utils.TimeUtils
import com.heyanle.easybangumi.ui.common.moeSnackBar
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.constant.EasyPlayerStatus
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IGestureComponent


/**
 * Create by heyanlin on 2022/11/2
 */
class StandardComponent: FrameLayout, IGestureComponent, SeekBar.OnSeekBarChangeListener {


    private val textMap = linkedMapOf<Float, TextView>().apply {
        put(3f, getTextView("x3.0", 3f))
        put(2f, getTextView("x2.0", 2f))
        put(1.5f, getTextView("x1.5", 1.5f))
        put(1f, getTextView("x1.0", 1f))
    }

    private var container: ComponentContainer? = null

    private var selectSpeedTextColor: Int = Color.Green.toArgb()
    private var normalSpeedTextColor: Int = Color.White.toArgb()

    private val binding: ComponentStandardBinding = ComponentStandardBinding.inflate(
        LayoutInflater.from(context), this, true)

    private var isLocked = false

    // seekbar 是否在滑动
    private var isSeekBarTouching = false

    // 是否在使用手势滑动进度
    private var isProgressSlide = false

    // 当前是否是可见状态（跟随 Controller 的状态，并不是真正的可不可见）
    private var isVisible = false

    private var playState: Int = EasyPlayStatus.STATE_IDLE

    init {
        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.ivFullscreen.setOnClickListener {
            runWithContainer {
                toggleFullScreen()
            }
        }
        binding.ivController.setOnClickListener {
            runWithContainer {
                togglePlay()
            }
        }
        binding.ivLock.setOnClickListener {
            runWithContainer {
                toggleLockState()
            }
        }
        binding.back.setOnClickListener {
            container?.stopFullScreen(true)
        }
        binding.seekBar.max = Int.MAX_VALUE
        EasyThemeController.curThemeColor?.let {
            val color = it.secondary.toArgb()
            val dra = binding.seekBar.progressDrawable as LayerDrawable
            dra.getDrawable(2).colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC)

            dra.findDrawableByLayerId(android.R.id.background).colorFilter = PorterDuffColorFilter(0x99ffffff.toInt(), PorterDuff.Mode.SRC)
            binding.seekBar.thumb.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            binding.seekBar.progressTintList = ColorStateList.valueOf(color)
            binding.seekBar.progressBackgroundTintList = ColorStateList.valueOf(Color.White.copy(0.6f).toArgb())
            binding.seekBar.secondaryProgressTintList = ColorStateList.valueOf(Color.White.toArgb())

            binding.progressBar.indeterminateTintList = ColorStateList.valueOf(color)
            binding.progressBar.indeterminateTintMode = PorterDuff.Mode.SRC_ATOP

            selectSpeedTextColor = it.secondary.toArgb()
        }
        textMap.iterator().forEach {
            binding.speedContainer.addView(it.value, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
        binding.speedRoot.visibility = View.GONE
        binding.speedRoot.setOnClickListener {
            hideSpeedContainer()
        }
        binding.tvSpeed.setOnClickListener {
            refreshSpeedContainer()
            showSpeedContainer()
        }
    }

    // == override IComponent

    override fun onPlayerStateChanged(playerState: Int) {
        when(playerState){
            EasyPlayerStatus.PLAYER_FULL_SCREEN -> {
                binding.upLayout.visibility = View.VISIBLE
            }
            else -> {
                binding.upLayout.visibility = View.GONE
            }
        }
    }

    override fun onPlayStateChanged(playState: Int) {
        Log.d("StandardComponent", "playState $playState")
        this.playState = playState
        isVisible = container?.isShowing()?:false
        onUIChange(isVisible, isLocked, playState)
//        refreshPlayPauseBtStatus()
//        runWithContainer {
//
//            // 进度管理
//            if(playState != EasyPlayStatus.STATE_PLAYING
//                && playState != EasyPlayStatus.STATE_BUFFERED
//                && playState != EasyPlayStatus.STATE_PREPARING
//            ){
//                stopProgressUpdate()
//            }else{
//                onProgressUpdate(duration = getDuration(), getCurrentPosition())
//                startProgressUpdate()
//            }
//
//
//            if(playState == EasyPlayStatus.STATE_PREPARING){
//                stopFadeOut()
//            }
//
//            if(playState != EasyPlayStatus.STATE_BUFFERING
//                && playState != EasyPlayStatus.STATE_PREPARING) {
//                binding.progressBar.visibility = View.GONE
//                binding.ivController.visibility = View.VISIBLE
//            }else{
//                binding.progressBar.visibility = View.VISIBLE
//                binding.ivController.visibility = View.GONE
//            }
//            if(playState == EasyPlayStatus.STATE_PREPARING){
//                binding.timelineLayout.visibility = View.GONE
//            }else{
//                binding.timelineLayout.visibility = View.VISIBLE
//            }
//
//        }
//
//        when(playState){
//            EasyPlayStatus.STATE_IDLE, EasyPlayStatus.STATE_PLAYBACK_COMPLETED -> {
//                // 复原
//                binding.seekBar.progress = 0
//                binding.seekBar.secondaryProgress = 0
//                //binding.root.visibility = View.GONE
//            }
//            EasyPlayStatus.STATE_BUFFERING -> {
//                binding.ivController.visibility = View.GONE
//                binding.progressBar.visibility = View.VISIBLE
//            }
////            EasyPlayStatus.STATE_PREPARING -> {
////                binding.ivController.visibility = View.GONE
////                binding.progressBar.visibility = View.VISIBLE
////                binding.timelineLayout.visibility = View.GONE
////                binding.root.visibility = View.VISIBLE
////            }
//
//            else -> {
//
//            }
//        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {

//        runWithContainer {
//            if(!isPlaying()){
//                stopFadeOut()
//            }
//        }
        //isVisible.toString().moeSnackBar()
        this.isVisible = isVisible
        onUIChange(isVisible, isLocked, playState)
//        if(!isProgressSlide && !isSeekBarTouching){
//            binding.ivLock.clearAnimation()
//            binding.contentLayout.clearAnimation()
//            if(isVisible){
//                binding.ivLock.startAnimation(showAnim)
//                binding.contentLayout.startAnimation(showAnim)
//            }else{
//                binding.ivLock.startAnimation(hideAnim)
//                binding.contentLayout.startAnimation(hideAnim)
//            }
//        }
    }

    private fun onUIChange(
        isVisible: Boolean,
        isLocked: Boolean,
        playState: Int,
    ){
        refreshPlayPauseBtStatus()
        runWithContainer {
            refreshTimeUI(getDuration(), getCurrentPosition())
            setSeekbarProgress(getDuration(), getCurrentPosition(), getBufferedPercentage())

            when (playState) {
                EasyPlayStatus.STATE_IDLE -> {
                    // 复原
                    binding.seekBar.progress = 0
                    binding.seekBar.secondaryProgress = 0
                    binding.root.visibility = View.GONE
                }
                // 加载资源中
                EasyPlayStatus.STATE_PREPARING -> {
                    // 加载中不显示加锁，显示进度条，进度条为 0,0 显示加载框 隐藏播放暂停按钮
                    // 强制显示，停止消失计时
                    binding.root.visibility = View.VISIBLE
                    binding.ivLock.hideWithAnim()
                    binding.contentLayout.showWithAnim()
                    binding.ivController.hideWithAnim()
                    if(isLocked){
                        setLocked(false)
                    }
                    if(!isVisible){
                        show()
                        stopFadeOut()
                    }
                    refreshTimeUI(0L, 0L)
                    binding.progressBar.showWithAnim()
                    stopFadeOut()
                }
                // 缓冲中
                EasyPlayStatus.STATE_BUFFERING -> {
                    binding.root.visibility = View.VISIBLE
                    if(isVisible){
                        if(isLocked){
                            binding.ivLock.showWithAnim()
                            binding.progressBar.hideWithAnim()
                            binding.contentLayout.hideWithAnim()
                            binding.ivController.hideWithAnim()
                        }else{
                            binding.ivLock.showWithAnim()
                            binding.progressBar.showWithAnim()
                            binding.contentLayout.showWithAnim()
                            binding.ivController.hideWithAnim()
                        }
                    }else{
                        binding.ivLock.hideWithAnim()
                        binding.contentLayout.hideWithAnim()
                        binding.progressBar.showWithAnim()
                        binding.ivController.hideWithAnim()
                    }
                }
                EasyPlayStatus.STATE_PLAYING -> {
                    binding.root.visibility = View.VISIBLE
                    binding.progressBar.hideWithAnim()

                    if(isVisible){
                        startFadeOut()
                        if(isLocked){
                            binding.ivLock.showWithAnim()
                            binding.contentLayout.hideWithAnim()
                            binding.ivController.hideWithAnim()
                        }else{
                            binding.ivLock.showWithAnim()
                            binding.contentLayout.showWithAnim()
                            binding.ivController.showWithAnim()
                        }
                    }else{
                        binding.ivLock.hideWithAnim()
                        binding.contentLayout.hideWithAnim()
                        binding.ivController.hideWithAnim()
                    }
                }
                EasyPlayStatus.STATE_PAUSED -> {
                    binding.root.visibility = View.VISIBLE
                    binding.progressBar.hideWithAnim()
                    binding.ivController.showWithAnim()
                    stopFadeOut()

                    if(isVisible){
                        if(isLocked){
                            binding.ivLock.showWithAnim()
                            binding.contentLayout.hideWithAnim()
                        }else{
                            binding.ivLock.showWithAnim()
                            binding.contentLayout.showWithAnim()
                        }
                    }else{
                        binding.ivLock.hideWithAnim()
                        binding.contentLayout.hideWithAnim()

                    }
                }
                EasyPlayStatus.STATE_ERROR -> {
                    binding.root.visibility = View.GONE
                }
            }
        }
    }

    private fun View.showWithAnim(){
        if(visibility == View.GONE){
            alpha = 0f
            val show = AlphaAnimation(0f, 1f).apply {
                duration = 100
                setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        alpha = 1f
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
            }
            clearAnimation()
            startAnimation(show)
        }else{
            alpha = 1f
        }
    }

    private fun View.hideWithAnim(){
        if(visibility == View.VISIBLE){
            alpha = 1f
            val hide = AlphaAnimation(1f, 0f).apply {
                duration = 100
                setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                        visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        alpha = 0f
                        visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }
                })
            }
            clearAnimation()
            startAnimation(hide)
        }else{
            alpha = 0f
        }
    }



    override fun onProgressUpdate(duration: Long, position: Long) {
        Log.d("StandardComponent", "onProgressUpdate dur->$duration pos->$position isProgressSlide->$isProgressSlide isSeekBarTouching->$isSeekBarTouching")
        if(!isProgressSlide && !isSeekBarTouching){
            refreshTimeUI(duration, position)
            setSeekbarProgress(duration, position, container?.getBufferedPercentage()?:0)
        }
    }

    override fun onLockStateChange(isLocked: Boolean) {
        this.isLocked = isLocked
        if(isLocked){
            binding.ivLock.setImageResource(R.drawable.ic_baseline_lock_24)
//            binding.contentLayout.visibility = View.GONE
//            runWithContainer {
//                startFadeOut()
//            }
        }else{
            binding.ivLock.setImageResource(R.drawable.ic_baseline_lock_open_24)
            // binding.contentLayout.visibility = View.VISIBLE
        }
        onUIChange(isVisible, isLocked, playState)
    }

    private fun getTextView(text: String, speed: Float): TextView{
        return TextView(context).apply {
            setPadding(0, dip2px(context, 8f), 0, dip2px(context, 8f))
            gravity = Gravity.CENTER
            this.text = text
            textSize = 18F
            setOnClickListener {
                runWithContainer {
                    setSpeed(speed = speed)
                }
                refreshSpeedContainer()
            }
        }
    }

    private fun refreshSpeedContainer(){
        runWithContainer {
            textMap.iterator().forEach {
                it.value.setTextColor(normalSpeedTextColor)
            }
            textMap[this.getSpeed()]?.setTextColor(selectSpeedTextColor)
        }
    }

    private fun showSpeedContainer(){
        runWithContainer {
            stopFadeOut()
        }
        binding.speedRoot.visibility = View.VISIBLE
        binding.speedScrollContainer.translationX = dip2px(context, 128F).toFloat()
        binding.speedScrollContainer.animate().translationX(0F).setListener(
            object: Animator.AnimatorListener{
                override fun onAnimationEnd(animation: Animator) {
                    binding.speedScrollContainer.translationX = 0F
                }

                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            }
        ).start()
    }
    private fun hideSpeedContainer(){
        runWithContainer {
            startFadeOut()
        }
        binding.speedRoot.visibility = View.VISIBLE
        binding.speedScrollContainer.translationX = 0F
        binding.speedScrollContainer.animate().translationX(dip2px(context, 128F).toFloat()).setListener(
            object: Animator.AnimatorListener{
                override fun onAnimationEnd(animation: Animator) {
                    binding.speedRoot.visibility = View.GONE
                    binding.speedScrollContainer.translationX = dip2px(context, 128F).toFloat()
                }

                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            }
        ).start()
    }


    override fun getView(): View {
        return this
    }

    override fun getLayoutParam(): RelativeLayout.LayoutParams {
        return RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
    }

    private fun requireContainer(): ComponentContainer {
        return container ?: throw NullPointerException()
    }

    private inline fun runWithContainer(block: ComponentContainer.()->Unit){
        container?.block()
    }

    override fun onAttachToContainer(container: ComponentContainer) {
        this.container = container
    }

    override fun onDetachToContainer(container: ComponentContainer) {
        this.container = null
    }

    // == override IGestureComponent

    override fun onSlidePositionChange(slidePosition: Long, currentPosition: Long, duration: Long) {
        Log.d("StandardComponent", "onSlidePositionChange($slidePosition,$currentPosition,$duration)")

        super.onSlidePositionChange(slidePosition, currentPosition, duration)
        if(isLocked){
            return
        }
        isProgressSlide = true
        runWithContainer {
            show()
            stopFadeOut()
            stopProgressUpdate()
            binding.progressBar.hideWithAnim()
            binding.ivController.hideWithAnim()
            seekTo(slidePosition)
            refreshTimeUI(duration, slidePosition)
            setSeekbarProgress(duration, slidePosition)
        }
    }

    override fun onStopSlide() {
        super.onStopSlide()
        isProgressSlide = false
        runWithContainer {
            onUIChange(isVisible, isLocked = isLocked, playState)
            startFadeOut()
            startProgressUpdate()
        }
    }

    // == override seekbar listener

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if(fromUser){
            runWithContainer {
                val newPosition = getDuration() * progress / seekBar.max
                seekTo(newPosition)
                refreshTimeUI(getDuration(), newPosition)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        isSeekBarTouching = true
        runWithContainer {
            stopFadeOut()
            stopProgressUpdate()
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        isSeekBarTouching = false
        runWithContainer {
            startFadeOut()
            startProgressUpdate()
            val newPosition = getDuration() * seekBar.progress / seekBar.max
            seekTo(newPosition)
        }
    }

    // == UI 显示效果控制

    private fun refreshTimeUI(duration: Long, position: Long){
        val durationStr = TimeUtils.toString(duration)
        val positionStr = TimeUtils.toString(position)
        binding.tvCurrentTime.text = positionStr
        binding.tvTotalTime.text = durationStr

    }

    private fun setSeekbarProgress(duration: Long, position: Long, bufferedPercentage:Int = 0 ){
        binding.seekBar.progress = ((position.toFloat()/duration)*binding.seekBar.max).toInt()
        binding.seekBar.secondaryProgress = (binding.seekBar.max/100F * bufferedPercentage).toInt()
    }

    private fun refreshPlayPauseBtStatus(){
        runWithContainer {
            if(isPlaying()){
                startFadeOut()
                binding.ivController.setImageResource(R.drawable.ic_baseline_pause_24)
            }else{
                stopFadeOut()
                binding.ivController.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            Log.d("StandardComponent", "isPlaying ${isPlaying()}")
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