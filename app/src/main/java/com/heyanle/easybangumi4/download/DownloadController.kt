package com.heyanle.easybangumi4.download

import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.m3u8.M3U8VodOption
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.base.db.dao.CartoonDownloadDao
import com.heyanle.easybangumi4.base.entity.CartoonDownload
import com.heyanle.easybangumi4.base.entity.CartoonInfo
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.jeffmony.m3u8library.VideoProcessManager
import com.jeffmony.m3u8library.listener.IVideoTransformListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URI
import java.net.URL
import java.security.spec.AlgorithmParameterSpec
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by HeYanLe on 2023/8/27 20:07.
 * https://github.com/heyanLE
 */
class DownloadController(
    private val cacheFile: File,
    private val downloadBus: DownloadBus,
    private val cartoonDownloadDao: CartoonDownloadDao,
    private val localCartoonController: LocalCartoonController,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(3)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mainScope = MainScope()

    private val aria = Aria.download(this)

    //private val d = M3U8VodOption

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
                    list.add("$host/$tspath")
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
        generateIndexFile()
    }

    fun newDownload(
        cartoonInfo: CartoonInfo,
        download: List<Triple<PlayLine, Int, PlayerInfo>>,
    ) {

        scope.launch {
            for(it in download){
                val index = it.second
                if(index < 0 || index >= it.first.episode.size){
                    continue
                }
                val episodeLabel = it.first.episode[index]
                val identify =   "${cartoonInfo.toIdentify()}-${it.first.label},${episodeLabel}"
                val taskId = aria.load(it.first.episode[it.second])
                    .setFilePath(File(cacheFile, "${identify}.source.m3u8").absolutePath)
                    .m3u8VodOption(m3u8Option)
                    .create()
                val cartoonDownload = CartoonDownload.fromCartoonInfo(cartoonInfo, taskId, it.first.label, episodeLabel, it.third)
                cartoonDownloadDao.insert(cartoonDownload)
            }
        }
    }

    // 解密
    private suspend fun decrypt(
        download: CartoonDownload
    ) {
        val entity = aria.getDownloadEntity(download.taskId).m3U8Entity
        if (entity == null) {
            updateDownload(
                download.copy(
                    status = -1,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error)
                )
            )
            return
        }
        val info = downloadBus.getInfo(download.toIdentify())

        val localM3U8 = File(entity.filePath ?: "")
        if (!localM3U8.exists() || !localM3U8.canRead()) {
            updateDownload(
                download.copy(
                    status = -1,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error)
                )
            )
            return
        }

        val target = File(cacheFile, "${download.toIdentify()}.m3u8.temp")
        val realTarget = File(cacheFile, "${download.toIdentify()}.m3u8")
        if (!target.createNewFile() || !target.canWrite()) {
            updateDownload(
                download.copy(
                    status = -1,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error)
                )
            )
            return
        }
        // 将本地 m3u8 文件中的 ts 全都解密改写成 tsh 文件
        // 输出新的 m3u8 文件，去除 key 标签，文件路径改为 tsh
        // 如果 tsh 文件的文件头是 png，则去除改文件头
        val it = localM3U8.readLines().iterator()
        val writer = target.writer(Charsets.UTF_8).buffered()
        val tsFiles = arrayListOf<File>()
        val targetTsFiles = arrayListOf<File>()
        while (it.hasNext()) {
            val line = it.next()
            if (line.startsWith("#EXTINF")) {
                if (!it.hasNext()) {
                    updateDownload(
                        download.copy(
                            status = -1,
                            errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error)
                        )
                    )
                    return
                } else {
                    val ts = it.next()
                    val file = File(ts)
                    val targetFile = File("${ts}h")
                    tsFiles.add(file)
                    // ts -> tsh
                    targetTsFiles.add(targetFile)

                    writer.write(line)
                    writer.newLine()
                    writer.write(targetFile.absolutePath)
                    writer.newLine()
                }

            } else if (line.startsWith("#EXT-X-KEY")) {
                // 新的 m3u8 文件不用解密了
                continue
            } else {
                writer.write(line)
                writer.newLine()
            }
        }
        writer.flush()
        info.status.value = stringRes(com.heyanle.easy_i18n.R.string.decrypting)
        // 开始解密咯！
        val needDecrypt = !entity.method.isNullOrEmpty()
        val keyFile = File(entity.filePath)
        val keyB = keyFile.readBytes()
        for (i in 0 until tsFiles.size.coerceAtMost(targetTsFiles.size)) {
            val ts = tsFiles[i]
            val tsh = targetTsFiles[i]
            tsh.createNewFile()
            if (!ts.canRead() || !tsh.canWrite()) {
                updateDownload(
                    download.copy(
                        status = -1,
                        errorMsg = stringRes(com.heyanle.easy_i18n.R.string.decrypt_error)
                    )
                )
                return
            }
            val s = ts.readBytes()
            val res = if (needDecrypt) decrypt(
                s,
                s.size,
                keyB,
                entity.iv,
                entity.method
            ) else s
            // 文件头伪装成 png
            val rr = res ?: s
            if (rr[0].toInt() == 0x89 && rr[1].toInt() == 0x50 && rr[2].toInt() == 0x4E && rr[3].toInt() == 0x47) {
                rr[0] = 0xff.toByte()
                rr[1] = 0xff.toByte()
                rr[2] = 0xff.toByte()
                rr[3] = 0xff.toByte()
            }
            tsh.writeBytes(rr)
        }

        target.renameTo(realTarget)

    }

    // 转码
    private suspend fun transcode(
        download: CartoonDownload
    ) {
        val info = downloadBus.getInfo(download.toIdentify())
        val m3u8 = File(cacheFile, "${download.toIdentify()}.m3u8")
        if (!m3u8.exists() || !m3u8.canRead()) {
            updateDownload(
                download.copy(
                    status = -1,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.transcode_error)
                )
            )
            return
        }
        val realTarget = localCartoonController.getTargetFile(download)
        val parentFile = realTarget.parentFile
        if (parentFile == null) {
            updateDownload(
                download.copy(
                    status = -1,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.transcode_error)
                )
            )
            return
        }
        val target = File(parentFile, realTarget.name + ".temp")
        if (!parentFile.exists()) {
            updateDownload(
                download.copy(
                    status = -1,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.transcode_error)
                )
            )
            return
        }
        mainScope.launch {
            info.status.value = stringRes(com.heyanle.easy_i18n.R.string.transcoding)
            info.subStatus.value = ""
        }
        // 开始转码
        VideoProcessManager.getInstance().transformM3U8ToMp4(
            m3u8.absolutePath,
            target.absolutePath,
            object : IVideoTransformListener {
                override fun onTransformProgress(progress: Float) {
                    mainScope.launch {
                        info.process.value = progress
                    }
                }

                override fun onTransformFailed(e: Exception?) {
                    e?.printStackTrace()
                    e?.message?.moeSnackBar()
                    scope.launch {
                        updateDownload(
                            download.copy(
                                status = -1,
                                errorMsg = stringRes(com.heyanle.easy_i18n.R.string.transcode_error)
                            )
                        )
                    }
                }

                override fun onTransformFinished() {
                    scope.launch {
                        target.renameTo(realTarget)
                        updateDownload(download.copy(status = 4))
                    }
                }
            }
        )
    }

    private suspend fun updateDownload(download: CartoonDownload) {
        cartoonDownloadDao.update(download)
    }

    /**
     * 解密ts
     *
     * @param sSrc   ts文件字节数组
     * @param length
     * @param sKey   密钥
     * @return 解密后的字节数组
     */
    @Throws(java.lang.Exception::class)
    private fun decrypt(
        sSrc: ByteArray,
        length: Int,
        sKey: ByteArray,
        iv: String,
        method: String
    ): ByteArray? {
        if (StringUtils.isNotEmpty(method) && !method.contains("AES")) return null
        // 判断Key是否为16位
        if (sKey.size != 16) {
            return null
        }
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec =
            SecretKeySpec(sKey, "AES")
        var ivByte: ByteArray
        ivByte =
            if (iv.startsWith("0x")) hexStringToByteArray(iv.substring(2)) else iv.toByteArray()
        if (ivByte.size != 16) ivByte = ByteArray(16)
        //如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
        val paramSpec: AlgorithmParameterSpec = IvParameterSpec(ivByte)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec)
        return cipher.doFinal(sSrc, 0, length)
    }

    private fun hexStringToByteArray(si: String): ByteArray {
        var s = si
        var len = s.length
        if (len and 1 == 1) {
            s = "0$s"
            len++
        }
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = (((s[i].digitToIntOrNull(16) ?: 0) shl 4) + (s[i + 1].digitToIntOrNull(16)
                ?: 0)).toByte()
            i += 2
        }
        return data
    }


}