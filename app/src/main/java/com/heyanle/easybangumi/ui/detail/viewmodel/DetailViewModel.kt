package com.heyanle.easybangumi.ui.detail.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi.entity.BangumiDetail

/**
 * Created by HeYanLe on 2021/9/21 13:57.
 * https://github.com/heyanLE
 */
class DetailViewModel : ViewModel() {

    val bangumiDetail = MutableLiveData<BangumiDetail>()

    val bangumiPlayMsg = MutableLiveData<LinkedHashMap<String, List<String>>>()

    val playEpisode = MutableLiveData<Pair<String, Int>>()

}