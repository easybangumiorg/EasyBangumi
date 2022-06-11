package com.heyanle.easybangumi.ui.detailplay.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi.entity.BangumiDetail

/**
 * Created by HeYanLe on 2021/9/21 13:57.
 * https://github.com/heyanLE
 */
class DetailPlayViewModel : ViewModel() {

    val bangumiDetail: MutableLiveData<BangumiDetail> = MutableLiveData()
    val playMsg: MutableLiveData<LinkedHashMap<String, List<String>>> = MutableLiveData()

    @Volatile var nowPlayLineIndex = 0
    @Volatile var nowPlayEpisode = 0
    @Volatile var playUrl: Array<Array<String>> = emptyArray()



}