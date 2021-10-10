package com.heyanle.easybangumi.source

import com.heyanle.easybangumi.source.bimibimi.BimibimiParser
import com.heyanle.easybangumi.source.yhdm.YhdmParser

/**
 * Created by HeYanLe on 2021/9/19 11:17.
 * https://github.com/heyanLE
 */
object ParserFactory {

    fun init(){
        register(BimibimiParser())
        register(YhdmParser())
    }

    private val parserMap = hashMapOf<String, IParser>()
    private val homeMap = hashMapOf<String, IHomeParser>()
    private val searchMap = hashMapOf<String, ISearchParser>()
    private val detailMap = hashMapOf<String, IBangumiDetailParser>()
    private val playMap = hashMapOf<String, IPlayUrlParser>()

    private fun register(iParser: IParser){
        parserMap[iParser.getKey()] = iParser
        if(iParser is IHomeParser){
            homeMap[iParser.getKey()] = iParser
        }
        if(iParser is ISearchParser){
            searchMap[iParser.getKey()] = iParser
        }
        if(iParser is IBangumiDetailParser){
            detailMap[iParser.getKey()] = iParser
        }
        if(iParser is IPlayUrlParser){
            playMap[iParser.getKey()] = iParser
        }

    }

    fun parser(key: String): IParser?{
        return parserMap[key]
    }

    fun homeKeys():List<String>{
        return homeMap.keys.toList()
    }

    fun searchKeys():List<String>{
        return searchMap.keys.toList()
    }

    fun home(key: String):IHomeParser?{
        return homeMap[key]
    }

    fun search(key: String): ISearchParser?{
        return searchMap[key]
    }

    fun detail(key: String): IBangumiDetailParser?{
        return detailMap[key]
    }

    fun play(key: String): IPlayUrlParser?{
        return playMap[key]
    }

}