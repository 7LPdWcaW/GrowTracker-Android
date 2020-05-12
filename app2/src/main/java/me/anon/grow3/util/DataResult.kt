package me.anon.grow3.util

/**
 * Wrapper class for LiveData objects.
 */
sealed class DataResult<out T>
{
	companion object
	{
		public fun <T> success(data: T): Success<T> = Success(data)
		public fun error(error: Throwable): Error = Error(error)
	}

	/**
	 * A successful response wrapper object with an embedded data type of <T>.
	 */
	data class Success<out T>(val data: T) : DataResult<T>()

	/**
	 * Error response wrapper
	 */
	open class Error(open val error: Throwable) : DataResult<Nothing>()

	/**
	 * A convenience class type that is returned when a resource is being loaded
	 */
	object Loading : DataResult<Nothing>()
}

/**
 * Convenience method to convert a generic response to the receiving data type, as itself.
 * This is an unsafe call so ensure the type is correct before calling
 */
public inline fun <reified T> DataResult<T>.asSuccess(): T = (this as DataResult.Success<T>).data

/**
 * Convenience method to convert a generic response to type [Error]. This is an unsafe call so ensure
 * the type is correct before calling
 */
public fun DataResult<*>.asFailure(): DataResult.Error = this as DataResult.Error

/**
 * Convenience method to convert a generic response to type [Loading]. This is an unsafe call so ensure
 * the type is correct before calling
 */
public fun DataResult<*>.asLoading(): DataResult.Loading = this as DataResult.Loading

public val DataResult<*>.isSuccess get() = this is DataResult.Success && data != null
public val DataResult<*>.isFailure get() = this is DataResult.Error
public val DataResult<*>.isLoading get() = this is DataResult.Loading
