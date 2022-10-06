package com.heyanle.easybangumi.anim.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heyanle.easy_daynight.ThemeManager
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ItemAnimHomeSourceTabBinding

/**
 * Created by HeYanLe on 2022/10/5 10:06.
 * https://github.com/heyanLE
 */
class AnimSourceTabView: RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        adapter = Adapter()
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        isNestedScrollingEnabled = false
    }

    private val realData = arrayListOf<String>()
    private var nowSelect = -1

    var onTabClick: (Int)->Boolean = {
        true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<String>){
        realData.clear()
        realData.addAll(data)
        nowSelect = 0
        adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelect(select: Int){
        nowSelect = select
        adapter?.notifyDataSetChanged()
    }


    inner class Adapter: RecyclerView.Adapter<AnimSourceTabView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAnimHomeSourceTabBinding.inflate(LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(realData[position], nowSelect == position)
            holder.binding.root.setOnClickListener {
                if(onTabClick(position)){
                    changeSelect(position)
                }
            }
        }

        override fun getItemCount(): Int {
            return realData.size
        }

    }

    class ViewHolder(val binding: ItemAnimHomeSourceTabBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String, isSelect: Boolean){
            binding.title.text = title
            if(isSelect){
                binding.title.setTextColor(Color.WHITE)
                binding.title.setBackgroundResource(R.drawable.anim_home_source_tab_back_select)
            }else{
                binding.title.setTextColor(ThemeManager.getAttrColor(binding.root.context, com.google.android.material.R.attr.subtitleTextColor))

                binding.title.background = null
            }
        }

    }

}