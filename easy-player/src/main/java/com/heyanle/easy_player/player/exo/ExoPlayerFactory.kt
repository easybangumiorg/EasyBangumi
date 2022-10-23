package com.heyanle.easy_player.player.exo

import android.content.Context
import com.heyanle.easy_player.player.IEasyPlayer
import com.heyanle.easy_player.player.IEasyPlayerFactory

/**
 * Created by HeYanLe on 2022/10/23 16:29.
 * https://github.com/heyanLE
 */
class ExoPlayerFactory : IEasyPlayerFactory{
    override fun createEasyPlayer(context: Context): IEasyPlayer {
        return ExoEasyPlayer(context)
    }
}