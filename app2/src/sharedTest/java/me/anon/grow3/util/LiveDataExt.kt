package me.anon.grow3.util

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking
import me.anon.grow3.util.states.DataResult
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isFailure
import me.anon.grow3.util.states.isSuccess
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
public fun <T> LiveData<T>.getOrAwaitValue(
	time: Long = 2,
	timeUnit: TimeUnit = TimeUnit.SECONDS,
	afterObserve: () -> Unit = {}
): T
{
	var data: T? = null
	val latch = CountDownLatch(1)
	val observer = object : Observer<T>
	{
		override fun onChanged(o: T?)
		{
			data = o
			latch.countDown()
			this@getOrAwaitValue.removeObserver(this)
		}
	}

	this.observeForever(observer)

	try
	{
		afterObserve.invoke()

		// Don't wait indefinitely if the LiveData is not set.
		if (!latch.await(time, timeUnit))
		{
			this.removeObserver(observer)
			throw TimeoutException("LiveData value was never set.")
		}

	}
	finally
	{
		this.removeObserver(observer)
	}

	@Suppress("UNCHECKED_CAST")
	return data as T
}

/**
 * Waits for a [DataResult] to return type of [DataResult.Success]
 */
public inline fun <reified J> LiveData<DataResult<J>>.awaitForSuccess(): J
{
	var result = getOrAwaitValue()
	while (!result.isSuccess)
	{
		runBlocking {
			result = getOrAwaitValue()
		}
	}

	return result.asSuccess()
}

/**
 * Waits for a [DataResult] to return type of [DataResult.Error]
 */
public inline fun <reified J> LiveData<DataResult<J>>.awaitForError(): J
{
	var result = getOrAwaitValue()
	while (!result.isFailure)
	{
		runBlocking {
			result = getOrAwaitValue()
		}
	}

	return result.asSuccess()
}

/**
 * Observes a [LiveData] until the `block` is done executing.
 */
public fun <T> LiveData<T>.observeForTesting(block: () -> Unit)
{
	val observer = Observer<T> { }
	try
	{
		observeForever(observer)
		block()
	}
	finally
	{
		removeObserver(observer)
	}
}
