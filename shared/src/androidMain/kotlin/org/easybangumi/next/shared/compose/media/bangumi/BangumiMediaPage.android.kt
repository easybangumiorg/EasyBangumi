package org.easybangumi.next.shared.compose.media.bangumi

actual class BangumiMediaPageParam(
    val bangumiMediaVM: AndroidBangumiMediaVM,
) {
    actual val commonVM: BangumiMediaCommonVM
        get() = bangumiMediaVM.commonVM
}