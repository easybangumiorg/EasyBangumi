package com.heyanle.app_download

import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.util.FileUtil
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern


/**
 * Created by HeYanLe on 2023/7/29 20:15.
 * https://github.com/heyanLE
 */

object DownloadController: DownloadTaskListener {

    data class DownloadItem(
        val url: String,
        val type: Int,
        val status: Int,
        val path: String,
        val m3u8Path: String,
        val taskId: Long,
    )

    private val _flow = MutableStateFlow<List<DownloadItem>>(listOf())
    val flow = _flow.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(3)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val map = ConcurrentHashMap<String, Job>()

    init {
        Aria.download(this).register()
    }


    private val m3u8Option = M3U8VodOption().apply {
        setVodTsUrlConvert { m3u8Url, tsUrls ->
            val list = arrayListOf<String>()
            val pattern = "[0-9a-zA-Z]+[.]ts"
            val r = Pattern.compile(pattern)
            for(i in tsUrls.indices){
                val tspath = tsUrls[i]
                if(tspath.startsWith("http://") ||tspath.startsWith("https://")){
                    list.add(tspath)
                }else if(r.matcher(tspath).find()){
                    val e = m3u8Url.lastIndexOf("/")+1
                    list.add(m3u8Url.substring(0, e)+tspath)
                }else{
                    val host = URI(m3u8Url).host
                    list.add(host + "/" + tspath)
                }
            }
            list
        }
        setBandWidthUrlConverter { m3u8Url, bandWidthUrl ->
            if(bandWidthUrl.startsWith("http://") || bandWidthUrl.startsWith("https://")){
                bandWidthUrl
            }else{
                val url = URL(m3u8Url)
                url.protocol + "://" + url.host + ":" + url.port+ "/" + bandWidthUrl
            }
        }
        setUseDefConvert(false)
        setMergeHandler { m3U8Entity, tsPath ->
            val sus = FileUtil.mergeFile(m3U8Entity.getFilePath(), tsPath)
            if(!sus){
                return@setMergeHandler false
            }
            val file = File(m3U8Entity.filePath)
            // 修改文件头
            true
        }
    }

    fun newTask(
        url: String,
        type: Int,
        folder: String,
        name: String,
    ){
        val taskId = Aria.download(this)
            .load(url)
            .let {
                if(type == 1){
                    it.m3u8VodOption(m3u8Option).setFilePath(File(folder, "$name.m3u8").absolutePath)
                }else{
                    it.setFilePath(File(folder, "$name.mp4").absolutePath)
                }
            }
            .create()
        val item = DownloadItem(
            url = url,
            type = type,
            status = 1,
            path = File(folder, "$name.mp4").absolutePath,
            m3u8Path = File(folder, "$name.m3u8").absolutePath,
            taskId = taskId,
        )
        _flow.update {
            it + item
        }
    }


    override fun onWait(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onPre(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskPre(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskResume(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskStart(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskStop(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskCancel(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskFail(task: DownloadTask?, e: Exception?) {
        //TODO("Not yet implemented")
    }

    override fun onTaskComplete(task: DownloadTask) {
        scope.launch {
            val old = _flow.value.find { it.taskId == task.entity.id } ?: return@launch
            DownloadBus.getInfo(task.downloadEntity.url).apply {
                status.value = 3
                speed.value = ""
                process.value = ""
            }
            FFmpegKit.execute("-allowed_extensions ALL -protocol_whitelist \"file,http,crypto,tcp\" -i ${old.m3u8Path} -c copy ${old.path}")
            DownloadBus.getInfo(task.downloadEntity.url).apply {
                status.value = 4
                speed.value = ""
                process.value = ""
            }
        }
    }

    override fun onTaskRunning(task: DownloadTask) {
        DownloadBus.getInfo(task.downloadEntity.url).apply {
            status.value = 2
            speed.value = task.downloadEntity.convertSpeed?:""
            process.value = task.convertCurrentProgress?:""
        }
    }

    override fun onNoSupportBreakPoint(task: DownloadTask?) {
        //TODO("Not yet implemented")
    }

    fun change(a: String, b: String){
        scope.launch {
            FFmpegKit.execute("-allowed_extensions ALL -protocol_whitelist \"file,http,crypto,tcp\" -i ${a} -vcodec copy -acodec copy ${b} -y")

        }

    }
}