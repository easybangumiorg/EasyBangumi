package com.heyanle.easy_bangumi_cm.room.media.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.heyanle.easy_bangumi_cm.room.media.entity.MediaInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaInfoDao {

    @Insert
    suspend fun insert(info: MediaInfo)


    @Query("select * from MediaInfo")
    fun flowAll(): Flow<List<MediaInfo>>

}