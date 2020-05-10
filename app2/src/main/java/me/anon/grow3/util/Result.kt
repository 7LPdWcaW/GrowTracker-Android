package me.anon.grow3.util

/**
 * // TODO: Add class description
 */
sealed class DataResult<T>
{
	data class Success<T>(val data: T) : DataResult<T>()
	data class Failure(val error: Exception) : DataResult<Nothing>()
	object Loading : DataResult<Nothing>()
}
