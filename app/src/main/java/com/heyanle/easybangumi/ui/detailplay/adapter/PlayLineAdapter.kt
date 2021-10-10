package com.heyanle.easybangumi.ui.detailplay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ItemPlayLineBinding
import com.heyanle.easybangumi.utils.getAttrColor

/**
 * Created by HeYanLe on 2021/10/5 21:34.
 * https://github.com/heyanLE
 */
class PlayLineAdapter (
    private val titleList: ArrayList<String>
        ) : RecyclerView.Adapter<PlayLineViewHolder>() {

    var onItemClickListener :(Int)->Unit = {}
    var selectTitle = ""

    fun selectTitle(title: String){
        val oldIndex = titleList.indexOf(selectTitle)
        selectTitle = title
        val newIndex = titleList.indexOf(selectTitle)
        notifyItemChanged(oldIndex)
        notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayLineViewHolder {
        return PlayLineViewHolder(ItemPlayLineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PlayLineViewHolder, position: Int) {
        holder.binding.playLine.text = titleList[position]
        holder.binding.sourceCard.setOnClickListener {
            onItemClickListener(position)
        }

        if(selectTitle == titleList[position]){
            holder.binding.sourceCard.setCardBackgroundColor(getAttrColor(holder.binding.root.context, R.attr.colorSecondary))
            holder.binding.playLine.setTextColor(getAttrColor(holder.binding.root.context, R.attr.colorOnSecondary))
        }else{
            holder.binding.sourceCard.setCardBackgroundColor(getAttrColor(holder.binding.root.context, R.attr.colorSurface))
            holder.binding.playLine.setTextColor(getAttrColor(holder.binding.root.context, R.attr.colorOnSurface))
        }

    }

    override fun getItemCount(): Int {
        return titleList.size
    }
}

class PlayLineViewHolder(val binding: ItemPlayLineBinding) :RecyclerView.ViewHolder(binding.root)
