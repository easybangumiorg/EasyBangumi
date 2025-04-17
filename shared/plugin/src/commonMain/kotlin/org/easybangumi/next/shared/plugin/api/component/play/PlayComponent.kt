//package org.easybangumi.next.shared.plugin.api.component.play
//
//
//
///**
// * Created by HeYanLe on 2024/12/8 22:05.
// * https://github.com/heyanLE
// */
//
//interface PlayComponent: PlayComponent {
//
//    suspend fun play(
//        cartoonIndex: CartoonIndex,
//        playerLine: PlayerLine,
//        episode: Episode,
//    ): SourceResult<PlayInfo>
//
//}
//
//fun ComponentBundle.playComponent(): PlayComponent?{
//    return this.getComponent(PlayComponent::class)
//}