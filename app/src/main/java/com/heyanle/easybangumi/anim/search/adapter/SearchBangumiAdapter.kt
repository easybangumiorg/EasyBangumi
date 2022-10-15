package com.heyanle.easybangumi.anim.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heyanle.easybangumi.databinding.ItemSearchAnimBinding
import com.heyanle.lib_anim.entity.Bangumi

/**
 * Created by HeYanLe on 2021/10/10 19:55.
 * https://github.com/heyanLE
 */
class SearchBangumiAdapter: PagingDataAdapter<Bangumi, SearchBangumiItemViewHolder>(
    DiffCallback()
) {

    private class DiffCallback: DiffUtil.ItemCallback<Bangumi>() {
        override fun areItemsTheSame(oldItem: Bangumi, newItem: Bangumi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bangumi, newItem: Bangumi): Boolean {
            return oldItem.id == newItem.id
        }
    }

    var onItemClickListener:(Int, Bangumi)->Unit = {_,_ ->}

    override fun onBindViewHolder(holder: SearchBangumiItemViewHolder, position: Int) {
        val binding = holder.binding
        getItem(position)?.let {
            Glide.with(binding.cover).load(it.cover).into(binding.cover)
            binding.tvTitle.text = it.name
            binding.intro.text = it.intro
            binding.root.setOnClickListener { _ ->
                onItemClickListener(position, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBangumiItemViewHolder {
        return SearchBangumiItemViewHolder(ItemSearchAnimBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
}

class SearchBangumiItemViewHolder(val binding: ItemSearchAnimBinding): RecyclerView.ViewHolder(binding.root)