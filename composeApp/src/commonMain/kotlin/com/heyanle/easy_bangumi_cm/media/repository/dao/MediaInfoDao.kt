package com.heyanle.easy_bangumi_cm.media.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.heyanle.easy_bangumi_cm.media.entity.MediaInfo
import com.heyanle.easy_bangumi_cm.media.entity.TestInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaInfoDao {

    @Insert
    fun insert(info: MediaInfoDao)

    @Insert
    fun insert(info: TestInfo)


    @Query("select * from MediaInfo")
    fun flowAll(): Flow<List<MediaInfo>>

    @Query("select * from TestInfo")
    fun flowTest(): Flow<List<TestInfo>>

}