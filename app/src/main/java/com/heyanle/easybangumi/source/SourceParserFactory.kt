package com.heyanle.easybangumi.source

import com.heyanle.easybangumi.source.agefans.AgefansParser
import com.heyanle.easybangumi.source.bimibimi.BimibimiParser
import com.heyanle.easybangumi.source.senfun.SenfunParser
import com.heyanle.easybangumi.source.yhdm.YhdmParser

/**
 * Created by HeYanLe on 2021/10/20 20:26.
 * https://github.com/heyanLE
 */
object SourceParserFactory {

    fun init(){
        register(BimibimiParser())
        register(YhdmParser())
        register(AgefansParser())
        register(SenfunParser())
    }

    private val parserMap = hashMapOf<String, ISourceParser>()
    private val homeMap = hashMapOf<String, IHomeParser>()
    private val searchMap = hashMapOf<String, ISearchParser>()
    private val detailMap = hashMapOf<String, IDetailParser>()
    private val playMap = hashMapOf<String, IPlayerParser>()

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

    fun homeLabel():List<String>{
        return homeMap.map {
            it.value.getLabel()
        }
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

    fun detail(key: String): IDetailParser?{
        return detailMap[key]
    }

    fun play(key: String): IPlayerParser?{
        return playMap[key]
    }
}