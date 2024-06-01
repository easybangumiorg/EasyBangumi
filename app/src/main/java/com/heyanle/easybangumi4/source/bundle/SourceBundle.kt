package com.heyanle.easybangumi4.source.bundle

import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.source.ConfigSource
import com.heyanle.easybangumi4.source.SourceInfo
import com.heyanle.easybangumi4.source_api.IconSource
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.component.update.UpdateComponent


/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */
class SourceBundle(
    list: List<ConfigSource>
) {

    companion object {
        val NONE = SourceBundle(emptyList())
    }


    private val sourceMap = linkedMapOf<String, SourceInfo.Loaded>()

    private val preferenceMap = linkedMapOf<String, PreferenceComponent>()

    private val iconMap = linkedMapOf<String, IconSource>()

    private val playMap = linkedMapOf<String, PlayComponent>()

    private val pageMap = linkedMapOf<String, PageComponent>()

    private val searchMap = linkedMapOf<String, SearchComponent>()

    private val detailedMap = linkedMapOf<String, DetailedComponent>()

    //private val migrateMap = linkedMapOf<String, MiSou>()

    init {
        list.filter {
            it.config.enable
        }.sortedBy {
            it.config.order
        }.map {
            it.sourceInfo
        }.filterIsInstance<SourceInfo.Loaded>().forEach {
            register(it)
        }
    }

    private fun register(sourceInfo: SourceInfo.Loaded) {
        val source = sourceInfo.source
        if (!sourceMap.containsKey(source.key)
            || sourceMap[source.key]!!.source.versionCode < source.versionCode
        ) {
            sourceMap[source.key] = sourceInfo



            iconMap.remove(source.key)
            playMap.remove(source.key)
            pageMap.remove(source.key)
            searchMap.remove(source.key)
            detailedMap.remove(source.key)
            preferenceMap.remove(source.key)

            if (source is IconSource) {
                iconMap[source.key] = source
            }


            val pageComponent = sourceInfo.componentBundle.getComponentProxy<PageComponent>()
            val detailedComponent = sourceInfo.componentBundle.getComponentProxy<DetailedComponent>()
            val playComponent = sourceInfo.componentBundle.getComponentProxy<PlayComponent>()
            val preferenceComponent = sourceInfo.componentBundle.getComponentProxy<PreferenceComponent>()
            val searchComponent = sourceInfo.componentBundle.getComponentProxy<SearchComponent>()
            val updateComponent = sourceInfo.componentBundle.getComponentProxy<UpdateComponent>()

            if (pageComponent != null) {
                pageMap[source.key] = pageComponent
            }

            if (detailedComponent != null) {
                detailedMap[source.key] = detailedComponent
            }

            if (playComponent != null) {
                playMap[source.key] = playComponent
            }

            if (preferenceComponent != null) {
                preferenceMap[source.key] = preferenceComponent
            }

            if (searchComponent != null) {
                searchMap[source.key] = searchComponent
            }

        }
    }


    fun sourceInfo(key: String) : SourceInfo? {
        return sourceMap[key]
    }

    fun sources(): List<Source> {
        val res = ArrayList<Source>()
        res.addAll(sourceMap.values.map { it.source })
        return res
    }

    fun source(key: String): Source? {
        return sourceMap[key]?.source
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

    fun preference(key: String): PreferenceComponent? {
        return preferenceMap[key]
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

    fun empty(): Boolean {
        return sourceMap.isEmpty()
    }

}