package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.plugin.source.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.plugin.source.js.source.JSComponentBundle
import com.heyanle.easybangumi4.plugin.source.js.source.JsSource
import com.heyanle.easybangumi4.plugin.source.ConfigSource
import com.heyanle.easybangumi4.plugin.source.ISourceController
import com.heyanle.easybangumi4.plugin.source.InnerSourceMaster
import com.heyanle.easybangumi4.plugin.source.SourceConfig
import com.heyanle.easybangumi4.plugin.source.SourceException
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.plugin.source.SourcePreferences
import com.heyanle.easybangumi4.plugin.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.SimpleComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.easybangumi4.plugin.source.js.JsSourceFileLoader
import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal data class SourceLoadCandidate(
    val file: File,
    val key: String?,
    val versionCode: Long,
)

internal fun selectHighestVersionCandidates(
    candidates: List<SourceLoadCandidate>,
): List<SourceLoadCandidate> {
    val selectedByKey = linkedMapOf<String, SourceLoadCandidate>()
    candidates.forEach { candidate ->
        val key = candidate.key?.takeIf { it.isNotBlank() } ?: return@forEach
        val current = selectedByKey[key]
        if (current == null || current.versionCode < candidate.versionCode) {
            selectedByKey[key] = candidate
        }
    }
    return candidates.filter { candidate ->
        val key = candidate.key?.takeIf { it.isNotBlank() } ?: return@filter true
        selectedByKey[key] === candidate
    }
}

class SourceController(
    private val sourceFolder: File,
    private val sourcePreferences: SourcePreferences,
    private val innerSourceFileProvider: InnerSourceFileProvider? = null,
) : ISourceController {

    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.newSingleDispatcher)
    private val jsRuntimeProvider = JSRuntimeProvider(2)
    private val componentBundleCache = ConcurrentHashMap<String, ComponentBundle>()
    // Installation and every reload share one transaction boundary. Otherwise an older scan can
    // finish after installation and publish a bundle that no longer contains the installed key.
    private val reloadMutex = Mutex()

    private val _sourceInfo = MutableStateFlow<ISourceController.SourceInfoState>(
        ISourceController.SourceInfoState.Loading
    )
    override val sourceInfo: StateFlow<ISourceController.SourceInfoState> = _sourceInfo

    private val _configSource = MutableStateFlow<List<ConfigSource>>(emptyList())
    override val configSource: StateFlow<List<ConfigSource>> = _configSource

    private val _sourceBundle = MutableStateFlow<SourceBundle?>(null)
    override val sourceBundle: StateFlow<SourceBundle?> = _sourceBundle

    init {
        sourceFolder.mkdirs()
        scope.launch {
            sourcePreferences.configs.requestFlow.distinctUntilChanged().collectLatest {
                reload()
            }
        }
    }

    suspend fun appendOrUpdateSource(file: File): DataResult<SourceFileInfo.Loaded> {
        return try {
            reloadMutex.withLock {
                val loaded = when (val info = loadSourceFile(file)) {
                    is SourceFileInfo.Loaded -> info
                    is SourceFileInfo.Error -> return@withLock DataResult.error(info.message, info.exception)
                    null -> return@withLock DataResult.error("source file cannot be loaded")
                }
                sourceFolder.mkdirs()
                val target = File(sourceFolder, "${loaded.key.toSafeFileName()}.${PluginV3.JS_SOURCE_SUFFIX.removePrefix(".")}")
                if (file.absolutePath != target.absolutePath) {
                    file.copyTo(target, overwrite = true)
                }
                componentBundleCache.remove(loaded.key)
                reloadLocked(sourcePreferences.configs.getOrDef())
                DataResult.ok(loaded.copy(file = target))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            DataResult.error(e)
        }
    }

    fun refresh() {
        scope.launch {
            reload()
        }
    }

    private suspend fun reload() = reloadMutex.withLock {
        // Read preferences after acquiring the lock: a queued refresh must not reapply a stale
        // enable/order snapshot over a newer install or preference update.
        reloadLocked(sourcePreferences.configs.getOrDef())
    }

    /** Caller must hold [reloadMutex]; do not call the locking reload from installation. */
    private suspend fun reloadLocked(configs: Map<String, SourceConfig>) {
        _sourceInfo.update { ISourceController.SourceInfoState.Loading }
        val configSources = loadSourceFiles(configs).toMutableList()
        currentCoroutineContext().ensureActive()
        configSources.add(InnerSourceMaster.localConfigSource)
        configSources.sortBy { it.config.order }
        _configSource.update { configSources }
        _sourceInfo.update {
            ISourceController.SourceInfoState.Info(configSources.map { source -> source.sourceInfo })
        }
        _sourceBundle.update { SourceBundle(configSources) }
    }

    private suspend fun loadSourceFiles(
        configs: Map<String, SourceConfig>,
    ): List<ConfigSource> = coroutineScope {
        val userFiles = sourceFolder.listFiles()
            ?.filter {
                it.isFile &&
                    it.name.endsWith(PluginV3.JS_SOURCE_SUFFIX) &&
                    !isBlockedSourceFile(it)
            }
            ?.sortedBy { it.name }
            .orEmpty()
        val innerFiles = innerSourceFileProvider?.loadSourceFiles()
            ?.filter { !isBlockedSourceFile(it) }
            .orEmpty()
        val files = selectHighestVersionCandidates(
            (userFiles + innerFiles).map { file ->
                async(Dispatchers.IO) {
                    val metadata = inspectSourceFile(file)
                    SourceLoadCandidate(
                        file = file,
                        key = metadata?.key,
                        versionCode = metadata?.versionCode ?: Long.MIN_VALUE,
                    )
                }
            }.awaitAll()
        ).map { it.file }

        files.map { file ->
            async(Dispatchers.IO) {
                when (val info = loadSourceFile(file)) {
                    is SourceFileInfo.Loaded -> load(info.source, configs[info.key])
                    is SourceFileInfo.Error -> ConfigSource(
                        SourceInfo.Error(
                            source = MetadataSource(
                                key = info.key.ifBlank { "file:${file.absolutePath}" },
                                label = info.label.ifBlank { file.name },
                                version = info.versionName,
                                versionCode = info.versionCode.toInt(),
                            ),
                            msg = info.message,
                            exception = info.exception as? Exception,
                        ),
                        configs[info.key] ?: SourceConfig(info.key, Int.MAX_VALUE, true),
                    )
                    null -> ConfigSource(
                        SourceInfo.Error(
                            source = MetadataSource(
                                key = "file:${file.absolutePath}",
                                label = file.name,
                                version = "",
                                versionCode = 0,
                            ),
                            msg = "source file cannot be loaded",
                        ),
                        SourceConfig("file:${file.absolutePath}", Int.MAX_VALUE, true),
                    )
                }
            }
        }.awaitAll()
    }

    private suspend fun load(source: Source, config: SourceConfig?): ConfigSource {
        if (config?.enable == false) {
            return ConfigSource(SourceInfo.Disabled(source), config)
        }

        val sourceConfig = config ?: SourceConfig(source.key, Int.MAX_VALUE, true)
        val cached = componentBundleCache[source.key]
        if (cached != null) {
            return ConfigSource(SourceInfo.Loaded(source, cached), sourceConfig)
        }

        val sourceInfo = try {
            val bundle = when (source) {
                is JsSource -> JSComponentBundle(source)
                else -> SimpleComponentBundle(source)
            }
            bundle.init()
            currentCoroutineContext().ensureActive()
            SourceInfo.Loaded(source, bundle).also {
                componentBundleCache[source.key] = bundle
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SourceException) {
            SourceInfo.Error(source, e.msg, e)
        } catch (e: Exception) {
            SourceInfo.Error(source, "load source failed: ${e.message}", e)
        }
        return ConfigSource(sourceInfo, sourceConfig)
    }

    private fun loadSourceFile(file: File): SourceFileInfo? {
        return JsSourceFileLoader(file, jsRuntimeProvider).load()
    }

    private fun inspectSourceFile(file: File): JsSourceFileLoader.Metadata? {
        return JsSourceFileLoader(file, jsRuntimeProvider).inspect()
    }

    private fun isBlockedSourceFile(file: File): Boolean {
        return file.name.startsWith(BLOCKED_SOURCE_FILE_PREFIX)
    }

    private class MetadataSource(
        override val key: String,
        override val label: String,
        override val version: String,
        override val versionCode: Int,
    ) : Source {
        override val describe: String? = null

        override fun register(): List<kotlin.reflect.KClass<*>> {
            return emptyList()
        }
    }

    private fun String.toSafeFileName(): String {
        return replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "source" }
    }

    private companion object {
        const val BLOCKED_SOURCE_FILE_PREFIX = "block-"
    }
}
