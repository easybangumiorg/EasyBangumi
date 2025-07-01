package org.easybangumi.next.shared.plugin.api.source

import kotlinx.coroutines.CoroutineScope
import org.easybangumi.next.lib.unifile.UFD
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by HeYanLe on 2024/12/8 21:00.
 * https://github.com/heyanLE
 */

interface Source {

    val manifest: SourceManifest

    val key: String get() = manifest.key

    val workPath: UFD

    val scope: CoroutineScope

    // source 内部为独立 koin，如果依赖外部 module 需要手动 includes
    val module: Module? get() = null

}