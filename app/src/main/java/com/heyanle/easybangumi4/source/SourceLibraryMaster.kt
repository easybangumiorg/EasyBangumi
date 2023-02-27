package com.heyanle.easybangumi4.source

import androidx.annotation.UiThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/22 21:43.
 * https://github.com/heyanLE
 */
object SourceLibraryMaster {

    val scope = MainScope()

    class SourceConfig(
        val key: String,
        val enable: Boolean,
        val order: Int,
    )
    
    private val sourceMap = MutableStateFlow(mapOf<String, Source>())
    private var parserConfigJsonOkkv by okkv("sourceConfigJsonOkkv", "[]")
    private var parserConfig = getOkkvConfig()


    @UiThread
    fun refreshSources(sources: Collection<Source>){
        scope.launch {
            val old = linkedMapOf<String, Source>()
            // old.putAll(sourceMap.value)
            sources.forEach {
                if (!old.containsKey(it.key)
                    || old[it.key]!!.versionCode < it.versionCode
                ) {
                    old[it.key] = it
                }
                it.loge("SourceLibraryMaster")
            }
            sourceMap.emit(old)
            old.iterator().forEach {
                if (!parserConfig.containsKey(it.key)) {
                    val config = SourceConfig(
                        it.key,
                        true,
                        parserConfig.size
                    )
                    parserConfig[it.key] = config
                }
            }

            val it = parserConfig.iterator()
            while(it.hasNext()){
                val d = it.next()
                if(!old.containsKey(d.key)){
                    it.remove()
                }
            }
            saveOkkv()
            saveNewConfig(parserConfig.values)
        }
    }

    @UiThread
    fun newSources(vararg source: Source){
        scope.launch {
            val old = linkedMapOf<String, Source>()
            old.putAll(sourceMap.value)
            source.forEach {
                if (!old.containsKey(it.key)
                    || old[it.key]!!.versionCode < it.versionCode
                ) {
                    old[it.key] = it
                }
            }
            sourceMap.emit(old)
            old.iterator().forEach {
                if (!parserConfig.containsKey(it.key)) {
                    val config = SourceConfig(
                        it.key,
                        true,
                        parserConfig.size
                    )
                    parserConfig[it.key] = config
                }
            }
            saveOkkv()
            saveNewConfig(parserConfig.values)
        }
    }

    fun saveConfig(config: SourceConfig){
        parserConfig[config.key] = config
        saveNewConfig(parserConfig.values)
    }

    @UiThread
    fun saveNewConfig(config: Collection<SourceConfig>) {
        val newConfigs = hashMapOf<String, SourceConfig>()
        val enableSource = arrayListOf<SourceConfig>()
        config.forEach {
            if (sourceMap.value.containsKey(it.key)) {
                newConfigs[it.key] = it
                if (it.enable) {
                    enableSource.add(it)
                }
            }
        }
        parserConfig.clear()
        parserConfig.putAll(newConfigs)
        saveOkkv()
        enableSource.sortBy {
            it.order
        }
        val sources = arrayListOf<Source>()
        val old = sourceMap.value
        enableSource.forEach { sourceConfig ->
            old[sourceConfig.key]?.let {
                sources.add(it)
            }
        }
        SourceMaster.newSource(SourceBundle(sources))
    }

    private fun getOkkvConfig(): HashMap<String, SourceConfig> {
        val res = hashMapOf<String, SourceConfig>()
        Gson().fromJson<List<SourceConfig>>(
            parserConfigJsonOkkv,
            object : TypeToken<List<SourceConfig>>() {}.type
        )
            .forEach {
                res[it.key] = it
            }
        return res
    }

    private fun saveOkkv() {
        parserConfigJsonOkkv = Gson().toJson(parserConfig.values)
    }

}