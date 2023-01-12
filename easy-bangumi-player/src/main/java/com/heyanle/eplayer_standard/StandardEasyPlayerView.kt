package com.heyanle.eplayer_standard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.heyanle.easy_bangumi_player.R
import com.heyanle.eplayer_core.easy_player.BaseEasyPlayerView

/**
 * Created by HeYanLe on 2023/1/12 20:01.
 * https://github.com/heyanLE
 */
class StandardEasyPlayerView: FrameLayout {
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


    var easyPlayerView: BaseEasyPlayerView =
        LayoutInflater.from(context).inflate(R.layout.view_easy_player, this, false) as BaseEasyPlayerView

    init {
        addView(easyPlayerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

}