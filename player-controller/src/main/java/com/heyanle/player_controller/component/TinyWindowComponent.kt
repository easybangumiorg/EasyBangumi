package com.heyanle.player_controller.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.heyanle.eplayer_core.controller.ComponentContainer
import com.heyanle.eplayer_core.controller.IComponent

/**
 * Created by HeYanLe on 2023/1/15 0:28.
 * https://github.com/heyanLE
 */
class TinyWindowComponent: FrameLayout, IComponent {


    override fun getView(): View? {
        return this
    }

    override fun onAttachToContainer(container: ComponentContainer) {
        TODO("Not yet implemented")
    }

    override fun onDetachToContainer(container: ComponentContainer) {
        TODO("Not yet implemented")
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