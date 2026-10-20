package com.heyanle.easybangumi4.cloudsync

import com.heyanle.easybangumi4.cloudsync.remote.RemoteWriteResult
import com.heyanle.easybangumi4.cloudsync.remote.CloudSyncRemoteException
import com.heyanle.easybangumi4.cloudsync.remote.WebDavConfig
import com.heyanle.easybangumi4.cloudsync.remote.WebDavRemoteStore
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebDavRemoteStoreTest {
    private lateinit var server: MockWebServer
    private lateinit var store: WebDavRemoteStore

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        store = WebDavRemoteStore(
            OkHttpClient(),
            WebDavConfig(
                directoryUrl = server.url("sync/").toString(),
                username = "user",
                password = "password",
            ),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun missingRemoteFileIsAValidEmptyState() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))

        assertNull(store.read())
        val request = server.takeRequest()
        assertEquals("/sync/easybangumi-sync-v1.json.gz", request.path)
        assertTrue(request.getHeader("Authorization")!!.startsWith("Basic "))
    }

    @Test
    fun updateUsesOptimisticConcurrencyHeader() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204).setHeader("ETag", "next"))

        val result = store.write(byteArrayOf(1, 2, 3), expectedVersion = "old")

        assertEquals(RemoteWriteResult.Success("next"), result)
        assertEquals("old", server.takeRequest().getHeader("If-Match"))
    }

    @Test
    fun existingFileWithoutEtagIsRejected() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("data"))

        val failure = runCatching { store.read() }.exceptionOrNull()

        assertTrue(failure is CloudSyncRemoteException)
        assertEquals(
            CloudSyncRemoteException.Kind.INVALID_RESPONSE,
            (failure as CloudSyncRemoteException).kind,
        )
    }

    @Test
    fun preconditionFailureBecomesConflict() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(412))

        assertEquals(RemoteWriteResult.Conflict, store.write(byteArrayOf(1), "old"))
    }
}
