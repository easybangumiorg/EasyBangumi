package com.heyanle.easy_bangumi_cm.plugin.api.component.media

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.component.MediaComponent
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonIndex
import com.heyanle.easy_bangumi_cm.model.cartoon.Episode
import com.heyanle.easy_bangumi_cm.model.cartoon.PlayInfo
import com.heyanle.easy_bangumi_cm.model.cartoon.PlayerLine


/**
 * Created by HeYanLe on 2024/12/8 22:05.
 * https://github.com/heyanLE
 */

interface PlayComponent: MediaComponent {

    suspend fun play(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): SourceResult<PlayInfo>

}

fun ComponentBundle.playComponent(): PlayComponent?{
    return this.getComponent(PlayComponent::class)
}