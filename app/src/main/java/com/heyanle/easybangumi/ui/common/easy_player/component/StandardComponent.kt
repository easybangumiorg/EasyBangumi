package com.heyanle.easybangumi.ui.common.easy_player.component

import android.R.color
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.compose.ui.graphics.toArgb
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ComponentStandardBinding
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.ui.common.easy_player.utils.TimeUtils
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IGestureComponent


/**
 * Create by heyanlin on 2022/11/2
 */
class StandardComponent: FrameLayout, IGestureComponent, SeekBar.OnSeekBarChangeListener {

    private var container: ComponentContainer? = null

    private val binding: ComponentStandardBinding = ComponentStandardBinding.inflate(
        LayoutInflater.from(context), this, true)

    private var isLocked = false

    // seekbar 是否在滑动
    private var isSeekBarTouching = false

    // 是否在使用手势滑动进度
    private var isProgressSlide = false

    // 当前是否是可见状态（跟随 Controller 的状态，并不是真正的可不可见）
    private var isVisible = false

    private val showAnim = AlphaAnimation(0f, 1f).apply {
        duration = 300
        setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.root.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }

    private val hideAnim = AlphaAnimation(1f, 0f).apply {
        duration = 300
        setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.root.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.root.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }

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
        binding.seekBar.max = Int.MAX_VALUE
        EasyThemeController.curThemeColor?.let {
            val color = it.secondary.toArgb()
            val dra = binding.seekBar.progressDrawable as LayerDrawable
            dra.getDrawable(2).colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC)

            dra.findDrawableByLayerId(android.R.id.background).colorFilter = PorterDuffColorFilter(0x99ffffff.toInt(), PorterDuff.Mode.SRC)
            binding.seekBar.thumb.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)

            binding.progressBar.indeterminateTintList = ColorStateList.valueOf(color)
            binding.progressBar.indeterminateTintMode = PorterDuff.Mode.SRC_ATOP

        }
    }

    // == override IComponent

    override fun onPlayerStateChanged(playerState: Int) {

    }

    override fun onPlayStateChanged(playState: Int) {
        Log.d("StandardComponent", "playState $playState")
        refreshPlayPauseBtStatus()
        runWithContainer {
            if(playState != EasyPlayStatus.STATE_PLAYING
                && playState != EasyPlayStatus.STATE_BUFFERED
                && playState != EasyPlayStatus.STATE_PREPARING
            ){
                stopProgressUpdate()
            }else{
                onProgressUpdate(duration = getDuration(), getCurrentPosition())
                startProgressUpdate()
            }

            if(playState == EasyPlayStatus.STATE_PREPARING){
                binding.root.visibility = View.VISIBLE
                stopFadeOut()
            }

            if(playState != EasyPlayStatus.STATE_BUFFERING
                && playState != EasyPlayStatus.STATE_PREPARING) {
                binding.progressBar.visibility = View.GONE
                binding.ivController.visibility = View.VISIBLE
            }else{
                binding.progressBar.visibility = View.VISIBLE
                binding.ivController.visibility = View.GONE
            }
            if(playState == EasyPlayStatus.STATE_PREPARING){
                binding.timelineLayout.visibility = View.GONE
            }else{
                binding.timelineLayout.visibility = View.VISIBLE
            }

        }

        when(playState){
            EasyPlayStatus.STATE_IDLE, EasyPlayStatus.STATE_PLAYBACK_COMPLETED -> {
                // 复原
                binding.seekBar.progress = 0
                binding.seekBar.secondaryProgress = 0
                binding.root.visibility = View.GONE
            }
            EasyPlayStatus.STATE_BUFFERING -> {
                binding.ivController.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
//            EasyPlayStatus.STATE_PREPARING -> {
//                binding.ivController.visibility = View.GONE
//                binding.progressBar.visibility = View.VISIBLE
//                binding.timelineLayout.visibility = View.GONE
//                binding.root.visibility = View.VISIBLE
//            }

            else -> {

            }
        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        runWithContainer {
            if(!isPlaying()){
                stopFadeOut()
            }
        }
        this.isVisible = isVisible
        if(!isProgressSlide && !isSeekBarTouching){
            binding.root.clearAnimation()
            if(isVisible){
                binding.root.startAnimation(showAnim)
            }else{
                binding.root.startAnimation(hideAnim)
            }
        }
    }

    override fun onProgressUpdate(duration: Long, position: Long) {
        Log.d("StandardComponent", "onProgressUpdate dur->$duration pos->$position isProgressSlide->$isProgressSlide isSeekBarTouching->$isSeekBarTouching")
        if(!isProgressSlide && !isSeekBarTouching){
            refreshTimeUI(duration, position)
            setSeekbarProgress(duration, position)
        }
    }

    override fun onLockStateChange(isLocked: Boolean) {
        this.isLocked = isLocked
        if(isLocked){
            binding.ivLock.setImageResource(R.drawable.ic_baseline_lock_24)
            binding.contentLayout.visibility = View.GONE
            runWithContainer {
                startFadeOut()
            }
        }else{
            binding.ivLock.setImageResource(R.drawable.ic_baseline_lock_open_24)
            binding.contentLayout.visibility = View.VISIBLE
        }
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
            stopFadeOut()
            stopProgressUpdate()
            binding.root.clearAnimation()
            binding.root.visibility = View.VISIBLE
            seekTo(slidePosition)
            refreshTimeUI(duration, slidePosition)
            setSeekbarProgress(duration, slidePosition)
        }
    }

    override fun onStopSlide() {
        super.onStopSlide()
        isProgressSlide = false
        runWithContainer {
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

    private fun setSeekbarProgress(duration: Long, position: Long ){
        binding.seekBar.progress = ((position.toFloat()/duration)*binding.seekBar.max).toInt()
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