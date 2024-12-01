package com.heyanle.easy_bangumi_cm.room.media.repository

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.heyanle.easy_bangumi_cm.room.media.entity.MediaInfo
import com.heyanle.easy_bangumi_cm.room.media.repository.dao.MediaInfoDao


@Database(entities = [MediaInfo::class], version = 1)
@ConstructedBy(MediaDatabaseConstructor::class)
abstract class MediaDatabase : RoomDatabase() {

    companion object {
        const val DB_FILE_NAME = "media.db"
    }

    abstract fun mediaInfoDao(): MediaInfoDao
}

expect fun onCreateMediaDatabase(): MediaDatabase

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MediaDatabaseConstructor : RoomDatabaseConstructor<MediaDatabase>
