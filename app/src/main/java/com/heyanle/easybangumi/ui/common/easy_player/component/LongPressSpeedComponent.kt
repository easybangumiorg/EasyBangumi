package com.heyanle.easybangumi.ui.common.easy_player.component

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.heyanle.easybangumi.databinding.ComponentLongPressBinding
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IGestureComponent

/**
 * Created by HeYanLe on 2022/11/7 23:12.
 * https://github.com/heyanLE
 */
class LongPressSpeedComponent : FrameLayout, IGestureComponent {

    companion object {
        private const val LONG_PRESS_SPEED = 2.0f
    }

    private var container: ComponentContainer? = null

    private val binding: ComponentLongPressBinding = ComponentLongPressBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private var isSpeed = false

    private var oldSpeed = 1f

    init {
        binding.root.visibility = View.GONE
    }

    override fun onLongPressStart() {
        super.onLongPressStart()
        runWithContainer {
            if (isPlaying()) {
                isSpeed = true
                oldSpeed = getSpeed()
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
                setSpeed(LONG_PRESS_SPEED * oldSpeed)
            }
        }
    }

    override fun onUp() {
        super.onUp()
        if (isSpeed) {
            isSpeed = false
            binding.root.clearAnimation()
            binding.root.alpha = 1f
            binding.root.animate().alpha(0f).setDuration(100)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.root.visibility = View.GONE
                    }
                }).start()
            runWithContainer {
                setSpeed(oldSpeed)
            }
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