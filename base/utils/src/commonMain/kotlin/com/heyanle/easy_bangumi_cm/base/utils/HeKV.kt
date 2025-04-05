package com.heyanle.easy_bangumi_cm.base.utils


import com.heyanle.easy_bangumi_cm.base.service.system.logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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

        private const val TAG = "HeKV"

        // 当写入记录条数为 map 数据条数多少倍时触发合并
        const val LOAD_FACTORY = 1.5f
    }

    private val flow = MutableStateFlow<Map<String, String>>(mapOf())
    private val readWriteLock = ReentrantReadWriteLock()

    private val directoryFile = File(path)
    private val journalFile = File(path, "${name}.journal")
    private val bkbFile = File(path, "${name}.journal.bkb")

    private val dispatcher = Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val init = CountDownLatch(1)

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
        logger.i(TAG + name, "${key}, ${value}")
        scope.launch {
            var m: Map<String, String>? = null
            if(value.isEmpty()){
                flow.update {
                    it.toMutableMap().apply {
                        remove(key)
                        m = this
                    }
                }
            }else{
                flow.update {
                    it.toMutableMap().apply {
                        put(key, value)
                        m = this
                    }
                }
            }
            readWriteLock.write {
                var lines = 0
                val mm = m ?: flow.value
                runCatching {
                    lines = append(key, value, journalFile)
                }.onFailure {
                    it.printStackTrace()
                }
                runCatching {
                    if(lines > mm.size* LOAD_FACTORY){
                        saveToFile(mm)
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    fun remove(key: String){
        init.await(500L, TimeUnit.MINUTES)
        put(key, "")
    }

    fun get(key: String, def: String): String{
        init.await(500L, TimeUnit.MINUTES)
        return flow.value.getOrDefault(key, def)
    }

    fun keys(): Set<String>{
        init.await(500L, TimeUnit.MINUTES)
        return flow.value.keys
    }

    fun map(): Map<String, String>{
        init.await(500L, TimeUnit.MINUTES)
        return flow.value.toMap()
    }

    // 如果被删除，视为存入空字符串
    fun flow(key: String, def: String): Flow<String> {
        init.await(500L, TimeUnit.MINUTES)
        return flow.map { it.getOrDefault(key, def) }
    }

    private fun saveToFile(map: Map<String, String>){
        readWriteLock.write {
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
    }

    private fun append(key: String, value: String, file: File): Int{
        if(!file.exists()){
            file.createNewFile()
        }
        if(file.exists()){
            // 写入新一行
            file.appendText("|${URLEncoder.encode(key, "utf-8")}|${URLEncoder.encode(value, "utf-8")}|\n")
        }
        return file.readLines().size
    }

    private fun initLoad(){
        if(journalFile.exists()){
            loadFromFile(journalFile)
        }else if(bkbFile.exists()){
            bkbFile.renameTo(journalFile)
            loadFromFile(journalFile)
        }
        init.countDown()
    }
    // 从文件中读数据到 map
    private fun loadFromFile(file: File){
        readWriteLock.read {
            val map = mutableMapOf<String, String>()
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
            flow.update {
                map
            }
        }
    }




}