package com.heyanle.easybangumi4.cloudsync

import com.heyanle.easybangumi4.cartoon.repository.CartoonInfoKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CloudSyncChangeBatch(
    val keys: Set<CartoonInfoKey>,
)

data class CloudSyncPendingChange(
    val generation: Long,
    val occurredAt: Long,
)

private data class QueuedCloudSyncChange(
    val key: CartoonInfoKey,
    val occurredAt: Long,
)

/**
 * Application-scoped change coalescer. It deliberately does not start network work; consumers can
 * observe [batches] later when automatic sync is introduced. Manual sync always performs a Room
 * reconciliation, so a process death between mutation and batching cannot lose user data.
 */
class CloudSyncEventBatcher {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val channel = Channel<QueuedCloudSyncChange>(Channel.UNLIMITED)
    private val mutex = Mutex()
    private val pending = linkedMapOf<CartoonInfoKey, CloudSyncPendingChange>()
    private var generation = 0L
    private val _pendingCount = MutableStateFlow(0)
    private val _batches = MutableSharedFlow<CloudSyncChangeBatch>(extraBufferCapacity = 1)

    val pendingCount = _pendingCount.asStateFlow()
    val batches = _batches.asSharedFlow()

    init {
        scope.launch {
            while (isActive) {
                val first = channel.receive()
                mutex.withLock {
                    pending[first.key] = CloudSyncPendingChange(++generation, first.occurredAt)
                    _pendingCount.value = pending.size
                }
                delay(BATCH_WINDOW_MILLIS)
                val batch = mutex.withLock {
                    while (true) {
                        val value = channel.tryReceive().getOrNull() ?: break
                        pending[value.key] = CloudSyncPendingChange(++generation, value.occurredAt)
                    }
                    _pendingCount.value = pending.size
                    CloudSyncChangeBatch(pending.keys.toSet())
                }
                _batches.emit(batch)
            }
        }
    }

    fun submit(keys: Collection<CartoonInfoKey>) {
        val now = System.currentTimeMillis()
        keys.distinct().forEach { channel.trySend(QueuedCloudSyncChange(it, now)) }
    }

    /** Returns a generation snapshot that can be acknowledged without losing later same-key edits. */
    suspend fun flush(): Map<CartoonInfoKey, CloudSyncPendingChange> = mutex.withLock {
        while (true) {
            val value = channel.tryReceive().getOrNull() ?: break
            pending[value.key] = CloudSyncPendingChange(++generation, value.occurredAt)
        }
        _pendingCount.value = pending.size
        pending.toMap()
    }

    suspend fun markUploaded(snapshot: Map<CartoonInfoKey, CloudSyncPendingChange>) = mutex.withLock {
        snapshot.forEach { (key, uploaded) ->
            if (pending[key]?.generation == uploaded.generation) pending.remove(key)
        }
        _pendingCount.value = pending.size
    }

    private companion object {
        const val BATCH_WINDOW_MILLIS = 350L
    }
}
