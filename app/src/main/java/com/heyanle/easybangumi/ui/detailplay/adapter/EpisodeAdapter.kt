package com.heyanle.easybangumi.ui.detailplay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ItemDetailEpisodeBinding
import com.heyanle.easybangumi.databinding.ItemEpisodeBinding
import com.heyanle.easybangumi.databinding.ItemPlayLineBinding
import com.heyanle.easybangumi.utils.getAttrColor

/**
 * Created by HeYanLe on 2021/10/5 21:44.
 * https://github.com/heyanLE
 */
class EpisodeAdapter (
    private val titleList: ArrayList<String>
) : RecyclerView.Adapter<EpisodeViewHolder>() {

    var onItemClickListener :(Int)->Unit = {}
    var nowSelectIndex = 0

    fun nowSelectIndex(index: Int){
        val old = nowSelectIndex
        nowSelectIndex = index
        notifyItemChanged(old)
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(ItemDetailEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.binding.episode.text = titleList[position]
        holder.binding.sourceCard.setOnClickListener {
            onItemClickListener(position)
        }
        if(nowSelectIndex == position){
            holder.binding.sourceCard.setCardBackgroundColor(getAttrColor(holder.binding.root.context, com.google.android.material.R.attr.colorSecondary))
            holder.binding.episode.setTextColor(getAttrColor(holder.binding.root.context, com.google.android.material.R.attr.colorOnSecondary))
        }else{
            holder.binding.sourceCard.setCardBackgroundColor(getAttrColor(holder.binding.root.context, com.google.android.material.R.attr.colorSurface))
            holder.binding.episode.setTextColor(getAttrColor(holder.binding.root.context, com.google.android.material.R.attr.colorOnSurface))
        }
    }

    override fun getItemCount(): Int {
        return titleList.size
    }
}

class EpisodeViewHolder(val binding: ItemDetailEpisodeBinding) : RecyclerView.ViewHolder(binding.root)