package me.anon.data.repository.impl

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.anon.data.repository.GardensRepository
import me.anon.data.source.GardensDataSource
import me.anon.model.Garden

/**
 * // TODO: Add class description
 */
class DefaultGardensRepository(
	private val dataSource: GardensDataSource,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GardensRepository
{
	override fun observeGardens(): LiveData<List<Garden>> = dataSource.observeGardens()
	override suspend fun getGardens(): List<Garden> = dataSource.getGardens()
}
