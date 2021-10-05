package com.heyanle.easybangumi.ui.detail.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ItemEpisodeBinding
import com.heyanle.easybangumi.utils.getAttrColor

/**
 * Created by HeYanLe on 2021/9/21 14:42.
 * https://github.com/heyanLE
 */
class EpisodeAdapter(
    private val list: List<String>
) : RecyclerView.Adapter<EpisodeViewHolder>() {

    var lastIndex = -1

    var onItemClickListener: (Int)->Unit = {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.itemEpisodeBinding.btn.text = list[position]
        if(position == lastIndex){
            holder.itemEpisodeBinding.btn.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    it.setBackgroundColor(it.resources.getColor(R.color.yellow_200, null))
                } else {
                    it.setBackgroundColor(it.resources.getColor(R.color.yellow_200))
                }
            }
        }else{
            holder.itemEpisodeBinding.btn.let {
                it.setBackgroundColor(getAttrColor(it.context, R.attr.colorSecondary))
            }
        }
        holder.itemEpisodeBinding.btn.setOnClickListener {
            onItemClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}

class EpisodeViewHolder(val itemEpisodeBinding: ItemEpisodeBinding) : RecyclerView.ViewHolder(
    itemEpisodeBinding.root
) {

}