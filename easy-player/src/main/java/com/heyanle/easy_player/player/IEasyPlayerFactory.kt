package com.heyanle.easy_player.player

import android.content.Context

/**
 * Created by HeYanLe on 2022/10/23 15:09.
 * https://github.com/heyanLE
 */
interface IEasyPlayerFactory {

    fun createEasyPlayer(context: Context): IEasyPlayer

}