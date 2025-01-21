package com.heyanle.easy_bangumi_cm.plugin.api.component.media

import com.heyanle.easy_bangumi_cm.plugin.api.component.EventComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.MediaComponent
import com.heyanle.easy_bangumi_cm.repository.cartoon.CartoonDetailed
import com.heyanle.easy_bangumi_cm.repository.cartoon.Episode
import com.heyanle.easy_bangumi_cm.repository.cartoon.PlayInfo
import com.heyanle.easy_bangumi_cm.repository.cartoon.PlayerLine

/**
 * Created by heyanlin on 2024/12/13.
 */
interface MediaEventComponent : MediaComponent, EventComponent {

    

    fun onPlayStart(
        cartoonDetailed: CartoonDetailed,
        playerLine: PlayerLine,
        episode: Episode,
        playInfo: PlayInfo
    ) {}

    /**
     * 播放结束（切集也会调用）
     * @param position 播放的位置
     * @param duration 播放的总时长
     */
    fun onPlayEnd(
        cartoonDetailed: CartoonDetailed,
        playerLine: PlayerLine,
        episode: Episode,
        playInfo: PlayInfo,
        position: Long,
        duration: Long,
    ) {}

}