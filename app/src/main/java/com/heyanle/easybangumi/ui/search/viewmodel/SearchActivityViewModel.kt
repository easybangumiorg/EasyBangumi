package com.heyanle.easybangumi.ui.search.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by HeYanLe on 2021/10/10 19:41.
 * https://github.com/heyanLE
 */
class SearchActivityViewModel : ViewModel() {

    val keyword:MutableLiveData<String> = MutableLiveData("")

}