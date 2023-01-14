package com.heyanle.player_controller.component

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IGestureComponent
import com.heyanle.player_controller.databinding.ComponentLongPressBinding

/**
 * Created by HeYanLe on 2022/11/7 23:12.
 * https://github.com/heyanLE
 */
class LongPressSpeedComponent: FrameLayout, IGestureComponent {

    companion object {
        private const val LONG_PRESS_SPEED = 2.0f
    }

    private var container: ComponentContainer? = null

    private val binding: ComponentLongPressBinding = ComponentLongPressBinding.inflate(
        LayoutInflater.from(context), this, true)

    private var isSpeed = false

    init{
        binding.root.visibility = View.GONE
    }

    override fun onLongPressStart() {
        super.onLongPressStart()
        runWithContainer {
            if(isPlaying()){
                isSpeed = true
                binding.root.visibility = View.VISIBLE
                binding.root.clearAnimation()
                binding.root.alpha = 0f
                binding.root.animate().alpha(1.0f).setDuration(100)
                    .setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.root.alpha = 1.0f
                        }
                    }).start()
                setSpeed(LONG_PRESS_SPEED)
            }
        }
    }

    override fun onUp() {
        super.onUp()
        if(isSpeed){
            isSpeed = false
            binding.root.clearAnimation()
            binding.root.alpha = 1f
            binding.root.animate().alpha(0f).setDuration(100)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.root.visibility = View.GONE
                    }
                }).start()
            runWithContainer {
                setSpeed(1.0f)
            }
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