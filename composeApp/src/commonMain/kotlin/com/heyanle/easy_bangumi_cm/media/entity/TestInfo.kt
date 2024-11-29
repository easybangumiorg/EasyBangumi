package com.heyanle.easy_bangumi_cm.media.entity

import androidx.room.Entity

@Entity(tableName = "TestInfo", primaryKeys = ["id"])
data class TestInfo(
    val id: String,
    val name: String,
)