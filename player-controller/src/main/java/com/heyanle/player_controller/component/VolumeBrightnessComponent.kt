package com.heyanle.player_controller.component

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IGestureComponent
import com.heyanle.player_controller.R
import com.heyanle.player_controller.databinding.ComponentVolumeBrightnessBinding

/**
 * Create by heyanlin on 2022/11/7
 */
class VolumeBrightnessComponent: FrameLayout, IGestureComponent {

    private var container: ComponentContainer? = null

    private val binding: ComponentVolumeBrightnessBinding = ComponentVolumeBrightnessBinding.inflate(
        LayoutInflater.from(context), this, true)

    override fun getView(): View {
        return this
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

    override fun onStartSlide() {
        super.onStartSlide()

    }

    override fun onBrightnessChange(percent: Int) {
        super.onBrightnessChange(percent)
        binding.centerContainer.alpha = 1.0f
        binding.centerContainer.clearAnimation()
        binding.centerContainer.visibility = View.VISIBLE
        runWithContainer {
            hide()
        }
        binding.ivIcon.setImageResource(R.drawable.ic_baseline_brightness_6_24)
        binding.tvPercent.text = buildString {
        append(percent)
        append("%")
    }
        binding.pbPercent.progress = percent
    }

    override fun onVolumeChange(percent: Int) {
        super.onVolumeChange(percent)
        binding.centerContainer.alpha = 1.0f
        binding.centerContainer.clearAnimation()
        binding.centerContainer.visibility = View.VISIBLE
        runWithContainer {
            hide()
        }
        if(percent <= 0){
            binding.ivIcon.setImageResource(R.drawable.ic_baseline_volume_off_24)
        }else if(percent <= 50){
            binding.ivIcon.setImageResource(R.drawable.ic_baseline_volume_down_24)
        }else {
            binding.ivIcon.setImageResource(R.drawable.ic_baseline_volume_up_24)
        }
        binding.tvPercent.text = buildString {
        append(percent)
        append("%")
    }
        binding.pbPercent.progress = percent
    }

    override fun onUp() {
        super.onUp()
        binding.centerContainer.animate()
            .alpha(0f).setDuration(300).setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.centerContainer.visibility = View.GONE
                }
            }).start()
    }

    override fun onPlayStateChanged(playState: Int) {
        super.onPlayStateChanged(playState)
        visibility =
            if (playState == EasyPlayStatus.STATE_IDLE ||
                playState == EasyPlayStatus.STATE_START_ABORT ||
                playState == EasyPlayStatus.STATE_PREPARING ||
                playState == EasyPlayStatus.STATE_PREPARED ||
                playState == EasyPlayStatus.STATE_ERROR ||
                playState == EasyPlayStatus.STATE_PLAYBACK_COMPLETED) {
                GONE
            } else {
                VISIBLE
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