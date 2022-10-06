package com.heyanle.easybangumi.anim.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.heyanle.easybangumi.databinding.ItemAnimHomeColumnBinding
import com.heyanle.lib_anim.entity.Bangumi

/**
 * Created by HeYanLe on 2022/10/5 10:50.
 * https://github.com/heyanLE
 */
class AnimHomeView: LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val viewHolders = arrayListOf<AnimHomeColumnViewL>()

    init {
        orientation = VERTICAL
    }


    fun setData(data: LinkedHashMap<String, List<Bangumi>>){
        refresh(data)
    }

    private fun makeSureColumnView(count: Int){
        while(viewHolders.size < count){
            val binding = ItemAnimHomeColumnBinding.inflate(LayoutInflater.from(context), this, false)
            viewHolders.add(AnimHomeColumnViewL(binding))
        }
    }

    private fun refresh(data: LinkedHashMap<String, List<Bangumi>>){
        removeAllViews()
        makeSureColumnView(data.size)
        val di = data.iterator()
        val hi = viewHolders.iterator()
        while(di.hasNext()){
            val d = di.next()
            val vl = hi.next()
            addView(vl.binding.root)
            vl.setData(d.value)
            vl.setTitle(d.key)
        }
    }





}