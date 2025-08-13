package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.js.source.JSComponentBundle
import com.heyanle.easybangumi4.plugin.js.source.JsSource
import com.heyanle.easybangumi4.plugin.source.bundle.SimpleComponentBundle
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.easybangumi4.plugin.source.debug.DebugSource
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.utils.TimeLogUtils
import com.heyanle.extension_api.NativeSupportedSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * 源业务层
 * Created by heyanlin on 2023/10/27.
 */
@Deprecated("SourceControllerV2")
class SourceController(
    private val extensionCase: ExtensionCase,
    private val sourcePreferences: SourcePreferences,
    private val cartoonInfoDao: CartoonInfoDao,
): ISourceController {

    companion object {
        val TAG = "SourceController"
    }

   
    private val _extensionSourceInfo = MutableStateFlow<ISourceController.SourceInfoState>(ISourceController.SourceInfoState.Loading)
    private val _innerSourceInfo = flow<ISourceController.SourceInfoState> {
        emit(ISourceController.SourceInfoState.Loading)
        val n = innerSource.map {
            loadSource(it)
        }
        emit(ISourceController.SourceInfoState.Info(n))
    }


    private val _sourceInfo = MutableStateFlow<ISourceController.SourceInfoState>(ISourceController.SourceInfoState.Loading)
    override val sourceInfo = _sourceInfo.asStateFlow()

    private val _configSource = MutableStateFlow<List<ConfigSource>>(emptyList())
    override val configSource = _configSource.asStateFlow()

    private val _sourceBundle = MutableStateFlow<SourceBundle?>(null)
    override val sourceBundle = _sourceBundle.asStateFlow()


    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val migrateScope = CoroutineScope(SupervisorJob() + dispatcher)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val innerSource = listOf<Source>(
        LocalSource,
        // DebugSource
    )



    init {
        scope.launch {
            extensionCase.flowExtensionState().collectLatest { sta ->
                if(sta.loading){
                    _sourceInfo.update {
                        ISourceController.SourceInfoState.Loading
                    }
                }else{
                    TimeLogUtils.i("loadSource start")
                    val it = sta.extensionInfoMap.values
                    val map = hashMapOf<String, Pair<ExtensionInfo.Installed, Source>>()
                    it.filterIsInstance<ExtensionInfo.Installed>().flatMap { exten ->
                        exten.sources.map {
                            exten to it
                        }
                    }.forEach {
                        val old = map[it.second.key]
                        if (old == null || old.second.versionCode <= it.second.versionCode) {
                            map[it.second.key] = it
                        }
                    }
                    val n = map.values.map {
                        val res = loadSource(it.second)
                        res
                    }
                    _extensionSourceInfo.update {
                        ISourceController.SourceInfoState.Info(n)
                    }
                }
            }
        }
//        scope.launch {
//            _sourceInfo.filterIsInstance<SourceInfoState.Info>()
//                .map { it.info.filterIsInstance<SourceInfo.Migrating>() }
//                .collectLatest {
//                    migrateScope.launch {
//                        it.forEach {
//                            migrate(it)
//                        }
//                    }
//                }
//        }
        scope.launch {
            combine(
                _extensionSourceInfo,
                _innerSourceInfo.distinctUntilChanged()
            ) { extensionSource, innerSource ->
                if (extensionSource is ISourceController.SourceInfoState.Loading || innerSource is ISourceController.SourceInfoState.Loading) {
                    ISourceController.SourceInfoState.Loading
                } else {
                    val e = extensionSource as ISourceController.SourceInfoState.Info
                    val i = innerSource as ISourceController.SourceInfoState.Info
                    ISourceController.SourceInfoState.Info(e.info + i.info)
                }

            }.collectLatest { n ->
                _sourceInfo.update {
                    n
                }
            }
        }
        scope.launch {
            combine(
                _sourceInfo.filterIsInstance<ISourceController.SourceInfoState.Info>().map {
                    it.info
                },
                sourcePreferences.configs.requestFlow.distinctUntilChanged()
            ) { sourceInfo, config ->
                val d = sourceInfo.map {
                    val con =
                        config[it.source.key] ?: SourceConfig(it.source.key, Int.MAX_VALUE, true)
                    ConfigSource(it, con)
                }
                d
            }.collectLatest { list ->
                _configSource.update {
                    list
                }
                _sourceBundle.update {
                    SourceBundle(list)
                }
            }
        }

    }

    private suspend fun loadSource(source: Source): SourceInfo {
        TimeLogUtils.i("loadSource ${source.key} start")
        return try {
            if (source is JsSource) {
                val bundle = JSComponentBundle(source)
                bundle.init()

            }
            val bundle =
                if (source is JsSource) JSComponentBundle(source) else SimpleComponentBundle(source)
            bundle.init()

//            // 加载 So 咯
            if (source is NativeSupportedSource) {
                return SourceInfo.Error(
                    source,
                    "NativeSupportedSource 已过时，请在 onInit 中加载 so"
                )
            }

            SourceInfo.Loaded(source, bundle)
        } catch (e: SourceException) {
            SourceInfo.Error(source,  e.msg, e)
        } catch (e: Exception) {
            e.printStackTrace()
            SourceInfo.Error(source, "加载错误：${e.message}", e)
        }
    }




}