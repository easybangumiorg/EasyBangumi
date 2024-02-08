package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.case.ExtensionCase
import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.source.bundle.ComponentBundle
import com.heyanle.easybangumi4.source.bundle.SourceBundle
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.TimeLogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 源业务层
 * Created by heyanlin on 2023/10/27.
 */
class SourceController(
    private val extensionCase: ExtensionCase,
    private val sourcePreferences: SourcePreferences,
    private val cartoonInfoDao: CartoonInfoDao,
) {

    sealed class SourceInfoState {
        data object Loading : SourceInfoState()

        class Info(val info: List<SourceInfo>) : SourceInfoState()
    }

    private val _sourceInfo = MutableStateFlow<SourceInfoState>(SourceInfoState.Loading)
    val sourceInfo = _sourceInfo.asStateFlow()

    private val _configSource = MutableStateFlow<List<ConfigSource>>(emptyList())
    val configSource = _configSource.asStateFlow()

    private val _sourceBundle = MutableStateFlow<SourceBundle?>(null)
    val sourceBundle = _sourceBundle.asStateFlow()


    private val dispatcher = CoroutineProvider.SINGLE
    private val migrateScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val scope = MainScope()

    init {
        scope.launch {
            extensionCase.flowExtensionState().collectLatest { sta ->
                if(sta.isLoading){
                    _sourceInfo.update {
                        SourceInfoState.Loading
                    }
                }else{
                    TimeLogUtils.i("loadSource start")
                    val it = sta.appExtensions.values + sta.fileExtension.values
                    val map = hashMapOf<String, Source>()
                    it.filterIsInstance<Extension.Installed>().flatMap {
                        it.sources
                    }.forEach {
                        val old = map[it.key]
                        if (old == null || old.versionCode <= it.versionCode) {
                            map[it.key] = it
                        }
                    }
                    val n = map.values.map {
                        loadSource(it)
                    }
                    _sourceInfo.update {
                        SourceInfoState.Info(n)
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
                _sourceInfo.filterIsInstance<SourceInfoState.Info>().map { it.info },
                sourcePreferences.configs.stateIn(scope)
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

    private fun loadSource(source: Source): SourceInfo {
        TimeLogUtils.i("loadSource ${source.key} start")
        return try {
            val bundle = ComponentBundle(source)
            bundle.init()
            SourceInfo.Loaded(source, bundle)
        } catch (e: SourceException) {
            SourceInfo.Error(source, e.msg, e)
        } catch (e: Exception) {
            e.printStackTrace()
            SourceInfo.Error(source, "加载错误：${e.message}", e)
        }
    }




}