package com.heyanle.easybangumi.ui.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.heyanle.easybangumi.entity.Bangumi

/**
 * Created by HeYanLe on 2021/9/20 22:35.
 * https://github.com/heyanLE
 */
class HomeFragmentViewModel : ViewModel() {

    val currentSourceIndex: MutableLiveData<Int> = MutableLiveData()

    val bangumiMap: MutableLiveData<LinkedHashMap<String, List<Bangumi>>> = MutableLiveData(LinkedHashMap())

}