package com.heyanle.easybangumi.anim.home.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heyanle.easybangumi.databinding.ItemAnimHomeBinding
import com.heyanle.easybangumi.databinding.ItemAnimHomeColumnBinding
import com.heyanle.lib_anim.entity.Bangumi

/**
 * Created by HeYanLe on 2022/10/5 10:52.
 * https://github.com/heyanLE
 */
class AnimHomeColumnViewL(
    val binding: ItemAnimHomeColumnBinding
){

    private val realData = arrayListOf<Bangumi>()
    var onItemClick: (Int)->Unit = {}

    init {
        binding.recycler.adapter = Adapter()
        binding.recycler.layoutManager = LinearLayoutManager(binding.recycler.context, RecyclerView.HORIZONTAL, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Bangumi>){
        realData.clear()
        realData.addAll(data)
        binding.recycler.adapter?.notifyDataSetChanged()
        binding.recycler.isNestedScrollingEnabled = false
    }

    fun setTitle(title: String){
        binding.columnTitle.text = title
    }

    inner class Adapter: RecyclerView.Adapter<AnimHomeColumnViewL.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAnimHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(realData[position])
            holder.binding.root.setOnClickListener {
                onItemClick(position)
            }
        }

        override fun getItemCount(): Int {
            return realData.size
        }
    }


    inner class ViewHolder(val binding: ItemAnimHomeBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(bangumi: Bangumi){
            Glide.with(binding.cover).load(bangumi.cover).into(binding.cover)
            binding.title.text = bangumi.name
            binding.intro.text = bangumi.intro
        }

    }



}