package com.heyanle.easybangumi.ui.common.easy_player.component

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.compose.ui.graphics.toArgb
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ComponentStandardBinding
import com.heyanle.easybangumi.databinding.ComponentTinyBinding
import com.heyanle.easybangumi.player.PlayerController
import com.heyanle.easybangumi.player.PlayerTinyController
import com.heyanle.easybangumi.theme.EasyThemeController
import com.heyanle.easybangumi.ui.common.easy_player.utils.TimeUtils
import com.heyanle.easybangumi.ui.player.BangumiPlayController
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IComponent

/**
 * Created by HeYanLe on 2023/1/15 0:28.
 * https://github.com/heyanLE
 */
@SuppressLint("ClickableViewAccessibility")
class TinyWindowComponent: FrameLayout, IComponent, SeekBar.OnSeekBarChangeListener {

    private var container: ComponentContainer? = null

    private val binding: ComponentTinyBinding = ComponentTinyBinding.inflate(
        LayoutInflater.from(context), this, true)

    // seekbar 是否在滑动
    private var isSeekBarTouching = false

    // 当前是否是可见状态（跟随 Controller 的状态，并不是真正的可不可见）
    private var isVisible = false

    private val showAnim = AlphaAnimation(0f, 1f).apply {
        duration = 300
        setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.contentLayout.visibility = View.VISIBLE
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
                binding.contentLayout.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.contentLayout.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }

    init {
        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.ivFullscreen.setOnClickListener {
            // todo 回到播放页面
            BangumiPlayController.getCurPendingIntent().send()
        }
        binding.ivClose.setOnClickListener {
            PlayerTinyController.dismissTiny()
            PlayerController.exoPlayer.pause()
        }
        binding.ivController.setOnClickListener {
            runWithContainer {
                togglePlay()
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
        binding.touchView.setOnTouchListener { v, event ->
            Log.d("Tiny", event.toString())
            if(!PlayerTinyController.onTouchEvent(event) && event.action == MotionEvent.ACTION_UP){
                container?.toggleShowState()
            }
            true
        }
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
                PlayerTinyController.dismissTiny()
                PlayerController.exoPlayer.pause()
            }
            EasyPlayStatus.STATE_BUFFERING -> {
                binding.ivController.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
            EasyPlayStatus.STATE_ERROR -> {
                PlayerTinyController.dismissTiny()
                PlayerController.exoPlayer.pause()
            }
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
        if( !isSeekBarTouching){
            binding.root.clearAnimation()
            if(isVisible){
                binding.root.startAnimation(showAnim)
            }else{
                binding.root.startAnimation(hideAnim)
            }
        }
    }
    override fun onProgressUpdate(duration: Long, position: Long) {
        if( !isSeekBarTouching){
            setSeekbarProgress(duration, position)
        }
    }

    // == override seekbar listener

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if(fromUser){
            runWithContainer {
                val newPosition = getDuration() * progress / seekBar.max
                seekTo(newPosition)
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

    override fun getView(): View {
        return this
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