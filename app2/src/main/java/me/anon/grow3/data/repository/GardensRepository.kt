package me.anon.grow3.data.repository

import androidx.lifecycle.LiveData
import me.anon.grow3.data.model.Garden
import me.anon.grow3.util.DataResult

/**
 * // TODO: Add class description
 */
interface GardensRepository
{
	public fun loaded(): LiveData<DataResult<Boolean>>
	public fun observeGardens(): LiveData<DataResult<List<Garden>>>
	suspend fun getGardens(): List<Garden>
}
