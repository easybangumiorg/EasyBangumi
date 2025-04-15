package org.easybangumi.next.lib.unifile

import okio.buffer
import okio.use
import org.easybangumi.next.lib.utils.FolderIndex
import org.easybangumi.next.lib.utils.copyTo
import org.easybangumi.next.lib.utils.pathProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

    @Test
    fun okio(){
        val ufd = pathProvider.getFilePath("test")
        print(ufd)
        val workerFolder = UniFileFactory.fromUFD(ufd)
        assertNotNull(workerFolder)

        val content = """
            testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest
            testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest
        """.trimIndent()

        val aTest = workerFolder.child("A")
        assertNotNull(aTest)
        aTest.delete()
        aTest.openSink(false).buffer().use {
            it.writeUtf8(content)
            it.flush()
        }

        val bTest = workerFolder.child("B")
        assertNotNull(bTest)
        bTest.delete()
        aTest.openSource().copyTo(bTest.openSink(true))

        val bTestSource = bTest.openSource().buffer()
        val bTestContent = bTestSource.readUtf8()

        assertEquals(content, bTestContent)

    }

    @Test
    fun folderIndexTest(){
//        val ufd = UFD(UFD.TYPE_JVM, "C:\\project\\android\\EasyBangumi\\EasyBangumi\\app")
//        val folder = UniFileFactory.fromUFD(ufd)
//        assertNotNull(folder)
//        val stringBuilder = StringBuilder()
//        val stringBuilder2 = StringBuilder()
//        FolderIndex.t(folder, stringBuilder)
//        FolderIndex.tt(folder, stringBuilder2)
//        assertEquals(stringBuilder.toString(), stringBuilder2.toString())

    }

}