package com.heyanle.easy_player.controller

/**
 * 将 IComponentContainer 和 IPlayerController 组合
 * @see IComponentContainer
 * @see IPlayerController
 * Created by HeYanLe on 2022/10/23 16:59.
 * https://github.com/heyanLE
 */
class ControllerWrapper(
    private val componentContainer: IComponentContainer,
    private val playerController: IPlayerController,
): IComponentContainer by componentContainer, IPlayerController by playerController {
}