package com.heyanle.easybangumi4.base.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heyanle.easybangumi4.base.entity.CartoonDownload
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/8/13 22:39.
 * https://github.com/heyanLE
 */
interface CartoonDownloadDao {

    @Insert
    suspend fun insert(cartoonDownload: CartoonDownload)

    @Update
    suspend fun update(cartoonDownload: CartoonDownload)

    @Delete
    suspend fun delete(cartoonDownload: CartoonDownload)

    @Query("SELECT * FROM cartoondownload")
    fun flowAll(): Flow<List<CartoonDownload>>

    @Query("DELETE FROM cartoonDownload WHERE downloadId=(:downloadId)")
    suspend fun deleteWithDownloadId(downloadId: String)

}