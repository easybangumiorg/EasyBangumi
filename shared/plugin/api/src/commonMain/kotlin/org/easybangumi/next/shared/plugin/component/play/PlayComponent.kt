package org.easybangumi.next.shared.plugin.component.play

import org.easybangumi.next.shared.plugin.component.ComponentBundle
import org.easybangumi.next.shared.plugin.component.PlayComponent
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.plugin.SourceResult


/**
 * Created by HeYanLe on 2024/12/8 22:05.
 * https://github.com/heyanLE
 */

interface PlayComponent: PlayComponent {

    suspend fun play(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): SourceResult<PlayInfo>

}

fun ComponentBundle.playComponent(): PlayComponent?{
    return this.getComponent(PlayComponent::class)
}