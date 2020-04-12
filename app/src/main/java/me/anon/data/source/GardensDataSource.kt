package me.anon.data.source

import androidx.lifecycle.LiveData
import me.anon.model.Garden

/**
 * // TODO: Add class description
 */
interface GardensDataSource
{
	public fun loaded(): LiveData<Result<Boolean>>
	public fun observeGardens(): LiveData<List<Garden>>

	suspend fun getGardens(): List<Garden>
}
