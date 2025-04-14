package org.easybangumi.next.shared.plugin.javascript

import okio.ByteString
import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.pathProvider
import kotlin.test.Test
import kotlin.test.assertContentEquals
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

class JsHelperTest {

    @Test
    fun test(){
        val ufd = pathProvider.getFilePath("test")
        print(ufd)
        val workerFolder = UniFileFactory.fromUFD(ufd)
        assertNotNull(workerFolder)

        // jsc test
        val jscTest = workerFolder.child("test.jsc")
        assertNotNull(jscTest)
        jscTest.delete()
        jscTest.openSink(false).buffer().use {
            it.write(JsHelper.FIRST_LINE_MARK)
            it.writeUtf8("test")
            it.flush()
        }

        val jsTest = workerFolder.child("test.js")
        assertNotNull(jsTest)
        jsTest.delete()
        jsTest.openSink(false).buffer().use {
            it.writeUtf8("test")
            it.flush()
        }

        val jscSource = jscTest.openSource().buffer()
        assertEquals(true, JsHelper.isSourceCry(jscSource))
        jscSource.skip(JsHelper.FIRST_LINE_MARK.size.toLong())

        val jscContent = jscSource.readUtf8()
        assertEquals("test", jscContent )

        val jsSource = jsTest.openSource().buffer()
        assertEquals(false, JsHelper.isSourceCry(jscSource))

        val jsContent = jsSource.readUtf8()
        assertEquals(jsContent, "test")


    }

}