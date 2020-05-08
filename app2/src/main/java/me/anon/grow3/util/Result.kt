package me.anon.grow3.util

/**
 * // TODO: Add class description
 */
sealed class DataResult<T>
{
	class Success<T>(val data: T)
	class Failure(val error: String)
	object Loading
}
