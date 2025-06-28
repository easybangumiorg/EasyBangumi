package org.easybangumi.next.shared.plugin.api.component.play

import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.Component


/**
 * Created by HeYanLe on 2024/12/8 22:05.
 * https://github.com/heyanLE
 */

interface PlayComponent: Component {

    suspend fun play(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode,
    ): SourceResult<PlayInfo>

}
