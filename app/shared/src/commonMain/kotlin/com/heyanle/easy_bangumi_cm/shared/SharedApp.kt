package com.heyanle.easy_bangumi_cm.shared

import com.heyanle.easy_bangumi_cm.room.roomModule
import org.koin.core.context.startKoin


/**
 * Created by HeYanLe on 2024/12/3 0:17.
 * https://github.com/heyanLE
 */

object SharedApp {

    fun init(){
        startKoin {
            modules(roomModule)
        }
    }


}