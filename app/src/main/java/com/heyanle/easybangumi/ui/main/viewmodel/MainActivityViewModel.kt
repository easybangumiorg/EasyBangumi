package com.heyanle.easybangumi.ui.main.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by HeYanLe on 2021/10/7 21:28.
 * https://github.com/heyanLE
 */
class MainActivityViewModel : ViewModel() {

    val refreshMyBangumi = MutableLiveData<Boolean>(false)
}