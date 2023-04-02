package com.heyanle.easybangumi4.source

import com.heyanle.bangumi_source_api.api.IconSource
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.component.Component
import com.heyanle.bangumi_source_api.api.component.detailed.DetailedComponent
import com.heyanle.bangumi_source_api.api.component.page.PageComponent
import com.heyanle.bangumi_source_api.api.component.play.PlayComponent
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent
import com.heyanle.bangumi_source_api.api.component.update.UpdateComponent
import com.heyanle.bangumi_source_api.api.configuration.ConfigSource
import com.heyanle.easybangumi4.utils.loge


/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */
class SourceBundle(
    list: List<Source>
) {

    private val sourceMap = linkedMapOf<String, Source>()

    private val configMap = linkedMapOf<String, ConfigSource>()

    private val iconMap = linkedMapOf<String, IconSource>()

    private val playMap = linkedMapOf<String, PlayComponent>()

    private val pageMap = linkedMapOf<String, PageComponent>()

    private val searchMap = linkedMapOf<String, SearchComponent>()

    private val detailedMap = linkedMapOf<String, DetailedComponent>()

    private val updateMap = linkedMapOf<String, UpdateComponent>()

    init {
        list.forEach {
            register(it)
        }
    }

    private fun register(source: Source) {
        if (!sourceMap.containsKey(source.key)
            || sourceMap[source.key]!!.versionCode < source.versionCode
        ) {
            sourceMap[source.key] = source

            if (source is ConfigSource) {
                configMap[source.key] = source
            }else{
                configMap.remove(source.key)
            }

            if (source is IconSource) {
                iconMap[source.key] = source
            }else{
                iconMap.remove(source.key)
            }

            playMap.remove(source.key)
            pageMap.remove(source.key)
            searchMap.remove(source.key)
            detailedMap.remove(source.key)
            updateMap.remove(source.key)


            val components = arrayListOf<Component>()

            (source as? Component)?.let {
                components.add(it)
            }
            components.addAll(source.components())

            components.forEach {

                if (it is PlayComponent) {
                    it.loge("SourceBundle")
                    playMap[it.source.key] = it
                }

                if(it is PageComponent) {

                    pageMap[it.source.key] = it
                }

                if(it is SearchComponent) {
                    searchMap[it.source.key] = it
                }

                if(it is DetailedComponent) {
                    it.loge("SourceBundle")
                    detailedMap[it.source.key] = it
                }

                if(it is UpdateComponent) {
                    updateMap[it.source.key] = it
                }
            }





        }
    }

    fun sources(): List<Source> {
        val res = ArrayList<Source>()
        res.addAll(sourceMap.values)
        return res
    }

    fun source(key: String): Source? {
        return sourceMap[key]
    }

    fun page(key: String): PageComponent? {
        return pageMap[key]
    }

    fun pages(): List<PageComponent> {
        return pageMap.toList().map {
            it.second
        }
    }

    fun search(key: String): SearchComponent? {
        return searchMap[key]
    }

    fun searches(): List<SearchComponent> {
        return searchMap.toList().map {
            it.second
        }
    }

    fun config(key: String): ConfigSource? {
        return configMap[key]
    }

    fun icon(key: String): IconSource? {
        return iconMap[key]
    }

    fun play(key: String): PlayComponent? {
        key.loge("SourceBundle")
        return playMap[key]
    }

    fun detailed(key: String): DetailedComponent? {
        key.loge("SourceBundle")
        return detailedMap[key]
    }

    fun update(key: String): UpdateComponent? {
        return updateMap[key]
    }

    fun empty(): Boolean {
        return sourceMap.isEmpty()
    }

}