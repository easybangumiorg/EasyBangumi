package com.heyanle.easy_bangumi_cm.room.media.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.heyanle.easy_bangumi_cm.room.media.entity.MediaInfo
import com.heyanle.easy_bangumi_cm.room.media.entity.TestInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaInfoDao {

    @Insert
    suspend fun insert(info: MediaInfo)

    @Insert
    suspend fun insert(info: TestInfo)


    @Query("select * from MediaInfo")
    fun flowAll(): Flow<List<MediaInfo>>

    @Query("select * from TestInfo")
    fun flowTest(): Flow<List<TestInfo>>

}