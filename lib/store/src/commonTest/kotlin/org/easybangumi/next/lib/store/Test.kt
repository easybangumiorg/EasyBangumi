package org.easybangumi.next.lib.store

import kotlinx.coroutines.runBlocking
import org.easybangumi.next.lib.store.file_helper.json.JsonFileHelper
import org.easybangumi.next.lib.utils.pathProvider
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class Test {


    data class TestData(
        val name: String,
        val age: Int,
    )

    @Test
    fun testJson() {
        val fileFolder = pathProvider.getFilePath("test")
        println(fileFolder)
        val fileHelper = JsonFileHelper<TestData>(
            fileFolder,
            "test",
            TestData::class,
            TestData("test", 1),
        )

        val data = fileHelper.getSync()
        assertEquals(data, TestData("test", 1))

        fileHelper.push(TestData("test", 2))

        val data2 = fileHelper.getSync()
        assertEquals(data2, TestData("test", 2))

    }

    @Test
    fun testJournal() {
        runBlocking {
            val fileFolder = pathProvider.getFilePath("test")
            println(fileFolder)
            val journalMapHelper = JournalMapHelper(
                fileFolder,
                "test",
            )

            repeat(1000) {
                journalMapHelper.putAndWait("k", it.toString())
            }

            val data = journalMapHelper.getSync("k")
            assertEquals(data, "999")

            repeat(2) {
                journalMapHelper.putAndWait("k", it.toString())
            }

            val data2 = journalMapHelper.getSync("k")
            assertEquals(data2, "1")
        }
    }

}