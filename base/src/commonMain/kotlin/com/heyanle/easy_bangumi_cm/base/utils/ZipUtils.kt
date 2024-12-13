package com.heyanle.easy_bangumi_cm.base.utils

import com.heyanle.easy_bangumi_cm.base.logi
import com.heyanle.easy_bangumi_cm.base.tlogi
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * Created by heyanlin on 2024/4/28.
 */
object ZipUtils {

    /**
     * 压缩文件和文件夹
     *
     * @param srcFilePath 要压缩的文件或文件夹
     * @param zipFilePath 压缩完成的Zip路径
     */
    suspend fun zipFolder(srcFilePath: String, zipFilePath: String): Boolean
        = withContext(CoroutineProvider.io) {
            kotlin.runCatching {
                //创建ZIP
                val outZip = ZipOutputStream(FileOutputStream(zipFilePath))
                //创建文件
                val file = File(srcFilePath)
                //压缩
                ("---- " + file.parent + "===" + file.absolutePath).tlogi()
                zipFiles(file.parent + File.separator, file.name, outZip)
                //完成和关闭
                outZip.finish()
                outZip.close()
            }.onFailure {
                it.printStackTrace()
            }.isFailure
        }



    /**
     * 压缩文件
     *
     * @param folderPath
     * @param filePath
     * @param zipOutputSteam
     */
    private fun zipFiles(
        folderPath: String,
        filePath: String,
        zipOutputSteam: ZipOutputStream?
    ) {
        (
                """
      folderString:$folderPath
      fileString:$filePath
      ==========================
      """.trimIndent()
                ).tlogi()
        if (zipOutputSteam == null) return
        val file = File(folderPath + filePath)
        if (file.isFile) {
            val zipEntry = ZipEntry(filePath)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while ((inputStream.read(buffer).also { len = it }) != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            //文件夹
            val fileList = file.list() ?: return
            //没有子文件和压缩
            if (fileList.isEmpty()) {
                val zipEntry = ZipEntry(filePath + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            //子文件和递归
            for (i in fileList.indices) {
                zipFiles("$folderPath$filePath/", fileList[i], zipOutputSteam)
            }
        }
    }
}