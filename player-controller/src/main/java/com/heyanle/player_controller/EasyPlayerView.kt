package com.heyanle.player_controller

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout

/**
 * Created by HeYanLe on 2023/1/14 23:24.
 * https://github.com/heyanLE
 */
class EasyPlayerView: FrameLayout {

    val basePlayerView: BaseEasyPlayerView = LayoutInflater.from(context).inflate(R.layout.easy_player, this, false) as BaseEasyPlayerView
    init {
        addView(basePlayerView)
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