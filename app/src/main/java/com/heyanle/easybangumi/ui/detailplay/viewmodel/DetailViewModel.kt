package com.heyanle.easybangumi.ui.detailplay.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi.entity.BangumiDetail

/**
 * Created by HeYanLe on 2021/9/21 13:57.
 * https://github.com/heyanLE
 */
class DetailPlayViewModel : ViewModel() {

    val bangumiDetail = MutableLiveData<BangumiDetail>()

    val bangumiPlayMsg = MutableLiveData<LinkedHashMap<String, List<String>>>(LinkedHashMap())



    val playPlayLine = MutableLiveData<String>("")
    val playEpisode = MutableLiveData<Int>(0)

    @Volatile var realPlayLine =""
    @Volatile var realEpisode = 0

}