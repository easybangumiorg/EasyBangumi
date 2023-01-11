package com.heyanle.easybangumi.ui.home

import com.heyanle.lib_anim.*
import com.heyanle.lib_anim.bimibimi.BimibimiParser
import com.heyanle.lib_anim.yhdm.YhdmParser


/**
 * Created by HeYanLe on 2022/9/18 15:54.
 * https://github.com/heyanLE
 */
object AnimSourceFactory {


    private val parserMap = hashMapOf<String, ISourceParser>()
    private val homeMap = hashMapOf<String, IHomeParser>()
    private val searchMap = hashMapOf<String, ISearchParser>()
    private val detailMap = hashMapOf<String, IDetailParser>()
    private val playMap = hashMapOf<String, IPlayerParser>()
    init {
        register(YhdmParser())
        register(BimibimiParser())
    }

    private fun register(iParser: ISourceParser){
        if(!parserMap.containsKey(iParser.getKey())
            || parserMap[iParser.getKey()]!!.getVersionCode() < iParser.getVersionCode()){
            parserMap[iParser.getKey()] = iParser
            if(iParser is IHomeParser){
                homeMap[iParser.getKey()] = iParser
            }
            if(iParser is ISearchParser){
                searchMap[iParser.getKey()] = iParser
            }
            if(iParser is IDetailParser){
                detailMap[iParser.getKey()] = iParser
            }
            if(iParser is IPlayerParser){
                playMap[iParser.getKey()] = iParser
            }
        }
    }

    fun parser(key: String): ISourceParser?{
        return parserMap[key]
    }

    fun homeKeys():List<String>{
        return homeMap.keys.toList()
    }



    fun labelsHome(): List<String>{
        val res = arrayListOf<String>()
        homeKeys().forEach { key ->
            parser(key)?.let {
                res.add(it.getLabel())
            }
        }
        return res
    }

    fun searchKeys():List<String>{
        return searchMap.keys.toList()
    }

    fun labelsSearch(): List<String>{
        val res = arrayListOf<String>()
        searchKeys().forEach { key ->
            parser(key)?.let {
                res.add(it.getLabel())
            }
        }
        return res
    }


    fun home(key: String):IHomeParser?{
        return homeMap[key]
    }

    fun search(key: String): ISearchParser?{
        return searchMap[key]
    }

    fun detail(key: String): IDetailParser?{
        return detailMap[key]
    }

    fun play(key: String): IPlayerParser?{
        return playMap[key]
    }


    fun requireHome(key: String): IHomeParser {
        return home(key) ?:throw NullPointerException("Home parser of key $key is null")
    }

    fun requireSearch(key: String): ISearchParser {
        return search(key) ?:throw NullPointerException("Search parser of key $key is null")
    }

    fun requireDetail(key: String): IDetailParser {
        return detail(key) ?: throw NullPointerException("Detail parser of key $key is null")
    }

    fun requirePlay(key: String): IPlayerParser {
        return play(key) ?: throw NullPointerException("Player parser of key $key is null")
    }

}

