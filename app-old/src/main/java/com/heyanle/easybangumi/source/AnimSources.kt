package com.heyanle.easybangumi.source

import com.heyanle.bangumi_source_api.api.*


/**
 * Created by HeYanLe on 2023/1/17 17:53.
 * https://github.com/heyanLE
 */
class AnimSources(
    list: List<ISourceParser>
) {
    private val parserMap = linkedMapOf<String, ISourceParser>()
    private val homeMap = linkedMapOf<String, IHomeParser>()
    private val searchMap = linkedMapOf<String, ISearchParser>()
    private val detailMap = linkedMapOf<String, IDetailParser>()
    private val playMap = linkedMapOf<String, IPlayerParser>()

    init {
        list.forEach {
            register(it)
        }
    }

    private fun register(iParser: ISourceParser) {
        if (!parserMap.containsKey(iParser.getKey())
            || parserMap[iParser.getKey()]!!.getVersionCode() < iParser.getVersionCode()
        ) {
            parserMap[iParser.getKey()] = iParser
            if (iParser is IHomeParser) {
                homeMap[iParser.getKey()] = iParser
            }
            if (iParser is ISearchParser) {
                searchMap[iParser.getKey()] = iParser
            }
            if (iParser is IDetailParser) {
                detailMap[iParser.getKey()] = iParser
            }
            if (iParser is IPlayerParser) {
                playMap[iParser.getKey()] = iParser
            }
        }
    }

    fun parser(key: String): ISourceParser? {
        return parserMap[key]
    }


    fun homeParsers(): List<IHomeParser> {
        val res = arrayListOf<IHomeParser>()
        for ((_, v) in homeMap) {
            res.add(v)
        }
        return res
    }

    fun searchParsers(): List<ISearchParser> {
        val res = arrayListOf<ISearchParser>()
        for ((_, v) in searchMap) {
            res.add(v)
        }
        return res
    }


    fun home(key: String): IHomeParser? {
        return homeMap[key]
    }

    fun search(key: String): ISearchParser? {
        return searchMap[key]
    }

    fun detail(key: String): IDetailParser? {
        return detailMap[key]
    }

    fun play(key: String): IPlayerParser? {
        return playMap[key]
    }

    fun empty(): Boolean {
        return parserMap.isEmpty()
    }


    fun requireHome(key: String): IHomeParser {
        return home(key) ?: throw NullPointerException("Home parser of key $key is null")
    }

    fun requireSearch(key: String): ISearchParser {
        return search(key) ?: throw NullPointerException("Search parser of key $key is null")
    }

    fun requireDetail(key: String): IDetailParser {
        return detail(key) ?: throw NullPointerException("Detail parser of key $key is null")
    }

    fun requirePlay(key: String): IPlayerParser {
        return play(key) ?: throw NullPointerException("Player parser of key $key is null")
    }
}