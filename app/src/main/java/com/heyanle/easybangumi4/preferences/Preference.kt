package com.heyanle.easybangumi4.preferences

import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Created by HeYanLe on 2023/3/22 15:58.
 * https://github.com/heyanLE
 */

// 无痕模式
object InPrivatePreferences: Preference<Boolean>("inPrivate", false)


open class Preference <T: Any> (
    key: String,
    def: T,
)  {


    private var okkv by okkv(key, def)

    private val _stateFlow = MutableStateFlow<T>(okkv)
    val stateFlow = _stateFlow.asStateFlow()

    suspend fun set(value: T){
        okkv = value
        _stateFlow.emit(value)
    }

}