package org.easybangumi.next.shared.plugin.api.source

import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.pathProvider


/**
 * Created by HeYanLe on 2024/12/8 21:00.
 * https://github.com/heyanLE
 */

interface Source {

    val manifest: SourceManifest

    val key: String get() = manifest.key

    val workPath: UFD


}