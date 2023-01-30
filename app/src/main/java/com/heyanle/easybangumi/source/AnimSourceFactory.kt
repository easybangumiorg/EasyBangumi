package com.heyanle.easybangumi.source

import com.heyanle.lib_anim.*
import com.heyanle.lib_anim.bimibimi.BimibimiParser
import com.heyanle.lib_anim.yhdm.YhdmParser
import com.heyanle.lib_anim.cycdm.CycdmParser
import com.heyanle.lib_anim.yhdmp.YhdmpParser
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * Created by HeYanLe on 2022/9/18 15:54.
 * https://github.com/heyanLE
 */
object AnimSourceFactory {

    // 把所有源作为一个 flow 对外暴露，全局可用
    private val animSourceFlow = MutableStateFlow(AnimSources(emptyList()))

    // 注册源
    // TODO 插件化加载
    fun init() {
        MainScope().launch {
            animSourceFlow.emit(
                AnimSources(
                    arrayListOf(
                        YhdmParser(),
                        BimibimiParser(),
                        CycdmParser(),
                        YhdmpParser(),

                    )
                )
            )
        }
    }

    fun label(key: String): String? {
        return animSourceFlow.value.parser(key)?.getLabel()
    }

    fun parsers(): Flow<AnimSources> {
        return animSourceFlow
    }

    fun homeParsers(): Flow<List<IHomeParser>> {
        return channelFlow {
            animSourceFlow.collectLatest {
                send(it.homeParsers())
            }
        }
    }

    fun searchParsers(): Flow<List<ISearchParser>> {
        return channelFlow {
            animSourceFlow.collectLatest {
                send(it.searchParsers())
            }
        }
    }

    fun home(source: String): IHomeParser? {
        return animSourceFlow.value.home(source)
    }

    fun search(source: String): ISearchParser? {
        return animSourceFlow.value.search(source)
    }

    fun detail(source: String): IDetailParser? {
        return animSourceFlow.value.detail(source)
    }

    fun play(source: String): IPlayerParser? {
        return animSourceFlow.value.play(source)
    }

    fun requireHome(key: String): IHomeParser {
        return animSourceFlow.value.requireHome(key)
    }

    fun requireSearch(key: String): ISearchParser {
        return animSourceFlow.value.requireSearch(key)
    }

    fun requireDetail(key: String): IDetailParser {
        return animSourceFlow.value.requireDetail(key)
    }

    fun requirePlay(key: String): IPlayerParser {
        return animSourceFlow.value.requirePlay(key)
    }

}

