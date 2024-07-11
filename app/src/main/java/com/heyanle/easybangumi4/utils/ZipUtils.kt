package com.heyanle.easybangumi4.utils

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
     * @param srcFileString 要压缩的文件或文件夹
     * @param zipFileString 压缩完成的Zip路径
     * @throws Exception
     */
    @Throws(Exception::class)
    fun zipFolder(srcFileString: String?, zipFileString: String?) {
        //创建ZIP
        val outZip = ZipOutputStream(FileOutputStream(zipFileString))
        //创建文件
        val file = File(srcFileString)
        //压缩
        ("---- " + file.parent + "===" + file.absolutePath).logi()
        zipFiles(file.parent + File.separator, file.name, outZip)
        //完成和关闭
        outZip.finish()
        outZip.close()
    }

    /**
     * 压缩文件
     *
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun zipFiles(
        folderString: String,
        fileString: String,
        zipOutputSteam: ZipOutputStream?
    ) {
        (
                """
      folderString:$folderString
      fileString:$fileString
      ==========================
      """.trimIndent()
                ).logi()
        if (zipOutputSteam == null) return
        val file = File(folderString + fileString)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileString)
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
            val fileList = file.list()
            //没有子文件和压缩
            if (fileList.size <= 0) {
                val zipEntry = ZipEntry(fileString + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            //子文件和递归
            for (i in fileList.indices) {
                zipFiles("$folderString$fileString/", fileList[i], zipOutputSteam)
            }
        }
    }
}