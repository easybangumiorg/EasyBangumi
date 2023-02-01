package com.heyanle.easybangumi.source

import androidx.annotation.UiThread
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heyanle.bangumi_source_api.api.*
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/2/1 16:29.
 * https://github.com/heyanLE
 */
object AnimSourceLibrary {

    val scope = MainScope()

    class SourceConfig(
        val key: String,
        val enable: Boolean,
        val order: Int,
    ){

    }

    private val parserMap = MutableStateFlow(mapOf<String, ISourceParser>())

    private var parserConfigJsonOkkv by okkv("parserConfigJsonOkkv", "[]")
    private var parserConfig = getOkkvConfig()

    fun getConfigs(): HashMap<String, SourceConfig> {
        val res = hashMapOf<String, SourceConfig>()
        parserMap.value.iterator().forEach {
            if(parserConfig.containsKey(it.key)){
                res[it.key] = parserConfig[it.key]?:throw java.lang.IllegalStateException()
            }else{
                val config = SourceConfig(it.key, false, parserConfig.size)
                parserConfig[it.key] = config
                res[it.key] = config
            }
        }
        saveOkkv()
        return res
    }


    @UiThread
    fun newSource(loader: ParserLoader, defaultEnable: Boolean){
        scope.launch {
            val old = linkedMapOf<String, ISourceParser>()
            old.putAll(parserMap.value)
            loader.load().forEach {
                if (!old.containsKey(it.getKey())
                    ||old[it.getKey()]!!.getVersionCode() < it.getVersionCode()
                ) {
                    old[it.getKey()] = it
                }
            }
            parserMap.emit(old)
            if(defaultEnable){
                old.iterator().forEach {
                    if(!parserConfig.containsKey(it.key)){
                        val config = SourceConfig(it.key, true, parserConfig.size)
                        parserConfig[it.key] = config
                    }
                }
                saveOkkv()
            }
            saveNewConfig(parserConfig.values.toList())
        }
    }


    @UiThread
    fun saveNewConfig(config: List<SourceConfig>){
        val newConfigs = hashMapOf<String, SourceConfig>()
        val enableSource = arrayListOf<SourceConfig>()
        config.forEach {
            if(parserMap.value.containsKey(it.key)){
                newConfigs[it.key] = it
                if(it.enable){
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
        val parser = arrayListOf<ISourceParser>()
        val old = parserMap.value
        enableSource.forEach { sourceConfig ->
            old[sourceConfig.key]?.let {
                parser.add(it)
            }
        }
        AnimSourceFactory.newSource(AnimSources(parser))
    }

    private fun getOkkvConfig(): HashMap<String,SourceConfig>{
        val res = hashMapOf<String, SourceConfig>()
        Gson().fromJson<List<SourceConfig>>(parserConfigJsonOkkv, object: TypeToken<List<SourceConfig>>(){}.type)
            .forEach {
                res[it.key] = it
            }
        return res
    }

    private fun saveOkkv(){
        parserConfigJsonOkkv = Gson().toJson(parserConfig.values)
    }

}