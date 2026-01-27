package org.easybangumi.next.shared.compose.media.bangumi

actual class BangumiMediaPageParam(
    val bangumiMediaVM: DesktopBangumiMediaVM,
) {
    actual val commonVM: BangumiMediaCommonVM
        get() = bangumiMediaVM.commonVM

}