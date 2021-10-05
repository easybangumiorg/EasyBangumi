package com.heyanle.easybangumi.ui.main.diy

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.LinearLayoutCompat

/**
 * Created by HeYanLe on 2021/9/21 21:31.
 * https://github.com/heyanLE
 */
class HomeColumnLinearLayout: LinearLayoutCompat {

    var onTouchDown : ()->Unit = {}
    var onTouchUpWithoutMove: ()->Unit = {}

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_DOWN){
            onTouchDown()
        }
        return super.dispatchTouchEvent(ev)
    }


}