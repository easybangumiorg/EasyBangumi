package com.heyanle.easybangumi.ui.common.easy_player.component

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.heyanle.easybangumi.databinding.ComponentLongPressBinding
import com.heyanle.easybangumi.databinding.ComponentSlideWinBinding
import com.heyanle.easybangumi.ui.common.easy_player.utils.TimeUtils
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IGestureComponent

/**
 * Created by HeYanLe on 2023/1/29 0:30.
 * https://github.com/heyanLE
 */
class SlideWindowComponent : FrameLayout, IGestureComponent {

    private var container: ComponentContainer? = null

    private val binding: ComponentSlideWinBinding = ComponentSlideWinBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        binding.root.visibility = View.GONE
    }

    override fun onStartSlide() {
        super.onStartSlide()

    }

    override fun onSlidePositionChange(slidePosition: Long, currentPosition: Long, duration: Long) {
        super.onSlidePositionChange(slidePosition, currentPosition, duration)
        showIfNeed()
        "${TimeUtils.toString(slidePosition)}/${TimeUtils.toString(duration)}".also {
            binding.root.text = it
        }
    }

    override fun onStopSlide() {
        super.onStopSlide()
        binding.root.clearAnimation()
        binding.root.alpha = 1f
        binding.root.animate().alpha(0f).setDuration(100)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.root.visibility = View.GONE
                }
            }).start()
    }

    fun showIfNeed() {
        if (binding.root.visibility != View.VISIBLE) {
            binding.root.visibility = View.VISIBLE
            binding.root.clearAnimation()
            binding.root.alpha = 0f
            binding.root.animate().alpha(1.0f).setDuration(100)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.root.alpha = 1.0f
                    }
                }).start()
        }
    }


    override fun getView(): View {
        return this
    }

    private inline fun runWithContainer(block: ComponentContainer.() -> Unit) {
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