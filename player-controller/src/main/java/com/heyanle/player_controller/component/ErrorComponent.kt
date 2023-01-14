package com.heyanle.player_controller.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IComponent
import com.heyanle.player_controller.databinding.ComponentErrorBinding

/**
 * Create by heyanlin on 2022/11/10
 */
class ErrorComponent: FrameLayout, IComponent {

    private val binding: ComponentErrorBinding = ComponentErrorBinding.inflate(LayoutInflater.from(context), this, false)

    private var container: ComponentContainer? = null

    init {
        addView(binding.root, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        binding.root.visibility = View.GONE
        binding.btRetry.setOnClickListener {
            binding.root.visibility = View.GONE
            runWithContainer {
                replay(false)
            }
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

    override fun onPlayStateChanged(playState: Int) {
        super.onPlayStateChanged(playState)
        if(playState == EasyPlayStatus.STATE_ERROR) {
            binding.root.visibility = View.VISIBLE
        }else {
            binding.root.visibility = View.GONE
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