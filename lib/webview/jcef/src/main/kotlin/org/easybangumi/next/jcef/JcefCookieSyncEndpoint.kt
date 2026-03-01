package org.easybangumi.next.jcef

import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

object JcefCookieSyncEndpoint {

    private data class Endpoint(
        val storageToJcef: suspend (String) -> Unit,
        val jcefToStorage: suspend () -> Unit,
    )

    private val endpointRef = AtomicReference<Endpoint?>(null)

    fun register(
        storageToJcef: suspend (String) -> Unit,
        jcefToStorage: suspend () -> Unit,
    ) {
        endpointRef.set(
            Endpoint(
                storageToJcef = storageToJcef,
                jcefToStorage = jcefToStorage,
            )
        )
    }

    fun unregister() {
        endpointRef.set(null)
    }

    suspend fun storageToJcef(url: String) {
        runCatching {
            endpointRef.get()?.storageToJcef?.invoke(url)
        }
    }

    suspend fun jcefToStorage() {
        runCatching {
            endpointRef.get()?.jcefToStorage?.invoke()
        }
    }

    fun storageToJcefBlocking(url: String) {
        runCatching {
            runBlocking {
                storageToJcef(url)
            }
        }
    }

    fun jcefToStorageBlocking() {
        runCatching {
            runBlocking {
                jcefToStorage()
            }
        }
    }
}
