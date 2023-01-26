package com.heyanle.easybangumi.ui.common.easy_player

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.heyanle.easybangumi.R

/**
 * Created by HeYanLe on 2023/1/15 19:17.
 * https://github.com/heyanLE
 */
class TinyEasyPlayerView : FrameLayout {

    val basePlayerView: BaseEasyPlayerView = LayoutInflater.from(context)
        .inflate(R.layout.easy_player_tiny, this, false) as BaseEasyPlayerView

    init {
        addView(basePlayerView)
        setBackgroundColor(Color.BLACK)
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