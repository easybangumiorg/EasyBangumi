package com.heyanle.easybangumi4.storage

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.repository.db.CartoonDatabase
import com.heyanle.easybangumi4.storage.entity.CartoonStorage
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Created by heyanle on 2024/7/26.
 * https://github.com/heyanLE
 */
object StorageMigrate {

    suspend fun migrate(folder: File, version: Int): Boolean {

        try {


            if (version < 86) {
                return false
            }

            if (version < 87) {
                val cartoonInfoFolder = File(folder, "cartoon_info")
                val dbFileO = File(cartoonInfoFolder, "easy_bangumi_cartoon.db")
                val dbFileShmO = File(cartoonInfoFolder, "easy_bangumi_cartoon.db-shm")
                val dbFileWalO = File(folder, "easy_bangumi_cartoon.db-wal")

                val dbFile = File(cartoonInfoFolder, "easy_bangumi_cartoon")
                val dbFileShm = File(cartoonInfoFolder, "easy_bangumi_cartoon-shm")
                val dbFileWal = File(cartoonInfoFolder, "easy_bangumi_cartoon-wal")

                dbFile.delete()
                dbFileShm.delete()
                dbFileWal.delete()
                dbFileO.renameTo(dbFile)
                dbFileShmO.renameTo(dbFileShm)
                dbFileWalO.renameTo(dbFileWal)
            }


            // 数据库 -> jsonl.gz
            if (version < 95) {
                val targetDB = File(folder, "easy_bangumi_cartoon")

                if (targetDB.exists()) {
                    return true
                }
                val database = CartoonDatabase.build(APP, targetDB.absolutePath)

                val cartoonInfo = database.cartoonInfoDao()
                val cartoonTagDao = database.cartoonTagDao()

                val tagLabelMap = hashMapOf<Int, String>()
                cartoonTagDao.getAll().forEach {
                    tagLabelMap[it.id] = it.label
                }

                val cartoonInfoFile = File(folder, "cartoon_info/cartoon_info_list.jsonl.gz")
                cartoonInfoFile.deleteRecursively()
                cartoonInfoFile.createNewFile()
                GZIPOutputStream(cartoonInfoFile.outputStream()).bufferedWriter().use { out ->
                    cartoonInfo.flowAll().first().forEach { info ->
                        val newTags = info.tagsIdList.map { tagLabelMap[it] }.joinToString(", ")
                        val storage = CartoonStorage.fromCartoonInfo(info, true, true).copy(
                            tags = newTags
                        )
                        out.write(storage.toJson())
                        out.newLine()
                    }
                    out.flush()
                }


            }

            // 在这里迁移

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}