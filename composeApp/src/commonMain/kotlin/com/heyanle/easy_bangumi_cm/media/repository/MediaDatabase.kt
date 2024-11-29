package com.heyanle.easy_bangumi_cm.media.repository

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.heyanle.easy_bangumi_cm.media.entity.MediaInfo
import com.heyanle.easy_bangumi_cm.media.entity.TestInfo
import com.heyanle.easy_bangumi_cm.media.repository.dao.MediaInfoDao


@Database(entities = [MediaInfo::class, TestInfo::class], version = 1)
@ConstructedBy(MediaDatabaseConstructor::class)
abstract class MediaDatabase : RoomDatabase() {

    companion object {
        const val DB_FILE_NAME = "media.db"
    }

    abstract fun mediaInfoDao(): MediaInfoDao
}

expect fun onCreateMediaDatabase(): MediaDatabase

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object MediaDatabaseConstructor : RoomDatabaseConstructor<MediaDatabase> {
    override fun initialize(): MediaDatabase
}