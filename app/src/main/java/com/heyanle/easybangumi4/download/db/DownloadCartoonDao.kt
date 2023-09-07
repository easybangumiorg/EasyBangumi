package com.heyanle.easybangumi4.download.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heyanle.easybangumi4.download.db.entity.DownloadCartoon
import kotlinx.coroutines.flow.Flow

/**
 * Created by HeYanLe on 2023/9/3 22:19.
 * https://github.com/heyanLE
 */
@Dao
interface DownloadCartoonDao {

    @Insert
    suspend fun insert(downloadCartoon: DownloadCartoon)

    @Update
    suspend fun update(downloadCartoon: DownloadCartoon)

    @Delete
    suspend fun remove(downloadCartoon: DownloadCartoon)

    @Query("SELECT * FROM downloadcartoon")
    fun flowAll(): Flow<List<DownloadCartoon>>

    @Query("SELECT * FROM downloadcartoon")
    suspend fun getAll(): List<DownloadCartoon>

}