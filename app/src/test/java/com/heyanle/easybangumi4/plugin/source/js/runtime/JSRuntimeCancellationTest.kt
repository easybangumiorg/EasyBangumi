package com.heyanle.easybangumi4.plugin.source.js.runtime

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSRuntimeCancellationTest {
    private class Queue {
        val tasks = ArrayDeque<Runnable>()
        fun post(task: Runnable) { tasks.addLast(task) }
        fun runNext() { tasks.removeFirst().run() }
    }

    private fun assertPropagatedException(expected: Throwable, actual: Throwable?) {
        val failure = requireNotNull(actual) { "Expected the posted task failure" }
        assertEquals(expected.javaClass, failure.javaClass)
        assertEquals(expected.message, failure.message)
        // Coroutine stacktrace recovery can copy the exception and retain the original as cause.
        assertTrue(generateSequence(failure) { it.cause }.any { it === expected })
    }

    @Test
    fun timeoutReturnsWithoutDrainingRuntimeQueueAndCancelledTaskIsSkipped() = runTest {
        val queue = Queue()
        var calls = 0
        val job = launch {
            withTimeout(100) { awaitPostedRuntimeTask(queue::post) { calls++ } }
        }
        runCurrent()
        assertEquals(1, queue.tasks.size)

        advanceTimeBy(101)
        runCurrent()
        assertTrue(job.isCancelled)
        assertTrue(job.isCompleted)
        queue.runNext()
        assertEquals(0, calls)
    }

    @Test
    fun propagatesNormalResultAndSkipsDuplicateDelivery() = runTest {
        val queue = Queue()
        var calls = 0
        val result = async { awaitPostedRuntimeTask(queue::post) { calls++; 42 } }
        runCurrent()
        val task = queue.tasks.removeFirst()
        task.run()
        task.run()
        assertEquals(42, result.await())
        assertEquals(1, calls)
    }

    @Test
    fun propagatesOriginalTaskException() = runTest {
        val queue = Queue()
        val expected = IllegalStateException("source failed")
        val result = async {
            runCatching { awaitPostedRuntimeTask(queue::post) { throw expected } }
        }
        runCurrent()
        queue.runNext()
        assertPropagatedException(expected, result.await().exceptionOrNull())
    }

    @Test
    fun discardsResultWhenCallerCancelsDuringSynchronousWork() = runTest {
        val queue = Queue()
        var delivered = false
        lateinit var job: Job
        job = launch {
            awaitPostedRuntimeTask(queue::post) { job.cancel(); 42 }
            delivered = true
        }
        runCurrent()
        queue.runNext()
        runCurrent()
        assertTrue(job.isCancelled)
        assertFalse(delivered)
    }

    @Test
    fun discardsLateTaskExceptionWhenCallerHasCancelled() = runTest {
        val queue = Queue()
        lateinit var job: Job
        job = launch {
            awaitPostedRuntimeTask(queue::post) {
                job.cancel()
                throw IllegalStateException("late failure after timeout")
            }
        }
        runCurrent()
        queue.runNext()
        runCurrent()
        assertTrue(job.isCancelled)
        assertTrue(job.isCompleted)
    }

    @Test
    fun cancellationBeforeDispatcherDeliveryDoesNotReturnCompletedValue() = runTest {
        val queue = Queue()
        var delivered = false
        val job = launch {
            awaitPostedRuntimeTask(queue::post) { 42 }
            delivered = true
        }
        runCurrent()
        queue.runNext()
        job.cancel()
        runCurrent()
        assertTrue(job.isCancelled)
        assertFalse(delivered)
    }

    @Test
    fun propagatesQueueSubmissionFailure() = runTest {
        val expected = IllegalStateException("queue unavailable")
        val result = runCatching {
            awaitPostedRuntimeTask<Unit>(post = { throw expected }) { error("must not run") }
        }
        assertPropagatedException(expected, result.exceptionOrNull())
    }
}
