package com.heyanle.easybangumi4.base.hekv

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * 在一个文件夹里存储 kv 结构，目前只能存 String，且无法存空字符串，存空字符串会被视为删除，需要外界自行转换
 * 使用 journal 缓存日志的形式保证数据稳定性
 * 最极端情况能保证只丢失最后一次写入记录
 * Created by HeYanLe on 2023/8/5 15:44.
 * https://github.com/heyanLE
 */
class HeKV(
    private val path: String,
    private val name: String, // 会占用该文件夹下的 {name}.journal {name}.journal.bkb
) {

    companion object {
        // 当写入记录条数为 map 数据条数多少倍时触发合并
        const val LOAD_FACTORY = 1.5f
    }

    private val map = HashMap<String, String>()
    private val readWriteLock = ReentrantReadWriteLock()

    private val directoryFile = File(path)
    private val journalFile = File(path, "${name}.journal")
    private val bkbFile = File(path, "${name}.journal.bkb")

    private val keyFlow = MutableSharedFlow<String>()


    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        runCatching {
            directoryFile.mkdirs()
        }.onFailure {
            it.printStackTrace()
        }
        scope.launch {
            initLoad()
        }
    }

    fun put(key: String, value: String){
        readWriteLock.write {
            if(value.isEmpty()){
                map.remove(key)
            }else{
                map[key] = value
            }
            scope.launch {
                var lines = 0
                runCatching {
                    lines = append(key, value, journalFile)
                }.onFailure {
                    it.printStackTrace()
                }
                runCatching {
                    keyFlow.tryEmit(key)
                }.onFailure {
                    it.printStackTrace()
                }
                runCatching {
                    if(lines > map.size* LOAD_FACTORY){
                        saveToFile()
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    fun remove(key: String){
        put(key, "")
    }

    fun get(key: String, def: String): String{
        return readWriteLock.read {
            map[key]?:def
        }
    }

    fun keys(): Set<String>{
        return map.keys
    }

    fun map(): Map<String, String>{
        return map.toMap()
    }

    // 如果被删除，视为存入空字符串
    fun flow(key: String, def: String): Flow<String> {
        return keyFlow
            .filter { it == key }
            .onStart { emit("ignition") }
            .map {
                get(key, def)
            }.conflate()
    }

    private fun saveToFile(){
        runCatching {
            bkbFile.delete()
            bkbFile.createNewFile()
            map.iterator().forEach {
                append(it.key, it.value, bkbFile)
            }
            journalFile.delete()
            bkbFile.renameTo(journalFile)
        }.onFailure {
            it.printStackTrace()
        }

    }

    private fun append(key: String, value: String, file: File): Int{
        if(!file.exists()){
            file.createNewFile()
        }
        if(file.exists()){
            // 写入新一行
            file.appendText("|${URLEncoder.encode(key, "utf-8")}|${URLEncoder.encode(value, "utf-8")}|")
        }
        return file.readLines().size
    }

    private fun initLoad(){
        readWriteLock.write {
            if(journalFile.exists()){
                loadFromFile(journalFile)
            }else if(bkbFile.exists()){
                bkbFile.renameTo(journalFile)
                loadFromFile(journalFile)
            }
        }
    }
    // 从文件中读数据到 map
    private fun loadFromFile(file: File){
        readWriteLock.write {
            map.clear()
            file.readLines().forEach { line ->
                // 一行完整的数据应该是 |key|value|
                // 被 | 分割成 4 部分，中间两部分是 key 和 value
                val ls = line.split("|")
                if(ls.size == 4){
                    val key = URLDecoder.decode(ls[1], "utf-8")
                    val value = URLDecoder.decode(ls[2], "utf-8")
                    map[key] = value
                }
            }

        }
    }




}