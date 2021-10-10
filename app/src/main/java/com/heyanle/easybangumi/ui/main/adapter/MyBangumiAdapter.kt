package com.heyanle.easybangumi.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.databinding.ItemMyFollowBangumiBinding
import com.heyanle.easybangumi.db.AppDatabase
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.ParserFactory
import com.heyanle.easybangumi.utils.TimeStringUtils

/**
 * Created by HeYanLe on 2021/10/7 20:43.
 * https://github.com/heyanLE
 */
class MyBangumiAdapter : PagingDataAdapter<BangumiDetail, MyBangumiItemViewHolder>(DiffCallback()){
    private class DiffCallback: DiffUtil.ItemCallback<BangumiDetail>() {
        override fun areItemsTheSame(oldItem: BangumiDetail, newItem: BangumiDetail): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BangumiDetail, newItem: BangumiDetail): Boolean {
            return oldItem.id == newItem.id
        }
    }

    var onItemClickListener:(Int, BangumiDetail)->Unit = {_,_ ->}
    var onItemMoreListener:(Int, BangumiDetail)->Unit = {_,_ ->}

    override fun onBindViewHolder(holder: MyBangumiItemViewHolder, position: Int) {
        val binding = holder.binding
        getItem(position)?.let {
            Glide.with(binding.cover).load(it.cover).into(binding.cover)
            binding.tvTitle.text = it.name

            val str = binding.intro.context.getString(R.string.last_watch_title, it.lastEpisodeTitle, TimeStringUtils.toTImeStringMill(it.lastProcessTime))
            binding.intro.text = binding.intro.context.getString(R.string.my_bangumi_item_intro, ParserFactory.parser(it.source)?.getLabel()?:"", it.intro, if(it.lastEpisodeTitle.isEmpty()) "" else str)
            binding.root.setOnClickListener { _ ->
                onItemClickListener(position, it)
            }
            binding.btMore.setOnClickListener { _ ->
                onItemMoreListener(position, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBangumiItemViewHolder {
        return MyBangumiItemViewHolder(ItemMyFollowBangumiBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
}



class MyBangumiItemViewHolder(val binding: ItemMyFollowBangumiBinding): RecyclerView.ViewHolder(binding.root)