package com.heyanle.easy_bangumi_cm.shared

import androidx.compose.runtime.Composable
import com.heyanle.easy_bangumi_cm.room.roomModule
import org.jetbrains.compose.resources.Resource
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


    @Composable
    fun Compose(){

    }


}