package org.easybangumi.next.shared.plugin.core.source.loader

import org.easybangumi.next.rhino.RhinoRuntimeProvider
import org.easybangumi.next.shared.plugin.api.source.SourceManifest
import org.easybangumi.next.shared.plugin.core.javascript.rhino.source.RhinoCryLoader
import org.easybangumi.next.shared.plugin.core.javascript.rhino.source.RhinoSourceLoader


private val JvmSourceLoaderFactory: SourceLoaderFactory by lazy {

    object : SourceLoaderFactory {

        private val rhinoRuntimeProvider = RhinoRuntimeProvider()

        private val rhinoSourceLoader = RhinoSourceLoader(rhinoRuntimeProvider)
        private val rhinoCryLoader = RhinoCryLoader(rhinoSourceLoader)

        override fun getLoader(loadType: Int): SourceLoader? {
            return when (loadType) {
                SourceManifest.LOAD_TYPE_JS -> rhinoSourceLoader
                SourceManifest.LOAD_TYPE_CRY_JS -> rhinoCryLoader
                else -> null
            }
        }
    }
}

actual fun getSourceLoaderFactory(): SourceLoaderFactory {
    return JvmSourceLoaderFactory
}