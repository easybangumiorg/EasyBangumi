package com.heyanle.easy_bangumi_cm.common.plugin.core.extension

import com.heyanle.easy_bangumi_cm.base.service.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.entity.ExtensionInfo
import com.heyanle.easy_bangumi_cm.common.plugin.core.extension.provider.JSFileExtensionProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.extension.provider.PkgExtensionProvider
import com.heyanle.easy_bangumi_cm.plugin.entity.ExtensionManifest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * ExtensionProvider1 ↘   拓展管理页面用         源加载用
 * ExtensionProvider2 → ExtensionManifest -> ExtensionInfo
 * ExtensionProvider3 ↗
 * Created by heyanlin on 2024/12/9.
 */
class ExtensionController(
    private val pathProvider: IPathProvider
) {

    // == ExtensionInfo Flow ================================================================================

    data class ExtensionInfoState(
        val loading: Boolean = true,
        val extensionInfoInfoMap: Map<String, ExtensionInfo> = emptyMap()
    )
    private val _infoState = MutableStateFlow<ExtensionInfoState>(
        ExtensionInfoState()
    )
    val infoState = _infoState.asStateFlow()



    // == ExtensionManifest Flow ================================================================================

    data class ExtensionManifestState(
        val loading: Boolean = true,
        val extensionManifest: Map<String, ExtensionManifest> = emptyMap()
    )
    private val _manifestState = MutableStateFlow<ExtensionManifestState>(
        ExtensionManifestState()
    )
    val manifestState = _manifestState.asStateFlow()



    // == 协程 ====================================================================================================

    private val singleScope = CoroutineScope(SupervisorJob() + CoroutineProvider.newSingle())
    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.io)

    // == ExtensionProvider ================================================================================

    private val workPath = pathProvider.getCachePath("extension")
    private val cachePath = pathProvider.getCachePath("extension")

    private val jsFileExtensionProvider = JSFileExtensionProvider(
        File(workPath, "js").absolutePath,
        singleScope,
    )

    private val pkgExtensionProvider = PkgExtensionProvider(
        File(workPath, "package").absolutePath,
        File(cachePath, "package").absolutePath,
        singleScope,
    )
    private var firstLoad = true


    init {
        // provider -> manifest
        scope.launch {
            combine(
                jsFileExtensionProvider.flow,
                pkgExtensionProvider.flow,
            ) { js, pkg ->
                if (firstLoad && (!js.loading && !pkg.loading)) {
                    firstLoad = false
                }
                val loading = firstLoad || (js.loading && pkg.loading)
                if (loading) {
                    return@combine ExtensionManifestState(
                        loading = true,
                        extensionManifest = emptyMap()
                    )
                }
                val map = mutableMapOf<String, ExtensionManifest>()
                js.extensionManifestList.forEach {
                    map[it.key] = it
                }
                pkg.extensionManifestList.forEach {
                    map[it.key] = it
                }
                ExtensionManifestState(
                    loading = false,
                    extensionManifest = map
                )
            }.collectLatest { i ->
                _manifestState.update { i }
            }

        }

    }

    fun uninstallExtension(extensionManifest: ExtensionManifest){

    }



}