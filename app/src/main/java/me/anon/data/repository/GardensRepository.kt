package me.anon.data.repository

import androidx.lifecycle.LiveData
import me.anon.model.Garden

/**
 * // TODO: Add class description
 */
interface GardensRepository
{
	public fun loaded(): LiveData<Result<Boolean>>
	public fun observeGardens(): LiveData<List<Garden>>
	suspend fun getGardens(): List<Garden>
}
