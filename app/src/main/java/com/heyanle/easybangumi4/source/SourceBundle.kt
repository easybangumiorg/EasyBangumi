package com.heyanle.easybangumi4.source

import com.heyanle.bangumi_source_api.api2.IconSource
import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.configuration.ConfigSource
import com.heyanle.bangumi_source_api.api2.page.PageSource
import com.heyanle.bangumi_source_api.api2.play.PlaySource
import com.heyanle.bangumi_source_api.api2.search.SearchSource

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */
class SourceBundle(
    list: List<Source>
) {

    private val sourceMap = linkedMapOf<String, Source>()

    private val pageMap = linkedMapOf<String, PageSource>()

    private val searchMap = linkedMapOf<String, SearchSource>()

    private val configMap = linkedMapOf<String, ConfigSource>()

    private val iconMap = linkedMapOf<String, IconSource>()

    private val playMap = linkedMapOf<String, PlaySource>()

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
            if (source is PageSource) {
                pageMap[source.key] = source
            } else {
                pageMap.remove(source.key)
            }

            if (source is SearchSource) {
                searchMap[source.key] = source
            } else {
                searchMap.remove(source.key)
            }

            if (source is ConfigSource) {
                configMap[source.key] = source
            } else {
                configMap.remove(source.key)
            }

            if (source is IconSource) {
                iconMap[source.key] = source
            } else {
                iconMap.remove(source.key)
            }

            if (source is PlaySource) {
                playMap[source.key] = source
            } else {
                playMap.remove(source.key)
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

    fun page(key: String): PageSource? {
        return pageMap[key]
    }

    fun search(key: String): SearchSource? {
        return searchMap[key]
    }

    fun config(key: String): ConfigSource? {
        return configMap[key]
    }

    fun icon(key: String): IconSource? {
        return iconMap[key]
    }

    fun play(key: String): PlaySource? {
        return playMap[key]
    }

    fun empty(): Boolean {
        return sourceMap.isEmpty()
    }

}