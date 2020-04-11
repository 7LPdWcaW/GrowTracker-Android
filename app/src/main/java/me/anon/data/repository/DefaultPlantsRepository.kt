package me.anon.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.anon.data.source.PlantsDataSource
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
class DefaultPlantsRepository(
	private val dataSource: PlantsDataSource,
	private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PlantsRepository
{
	override suspend fun reload()
	{
		dataSource.getPlants()
	}

	override fun observePlants(): LiveData<List<Plant>> = dataSource.observePlants()
	override suspend fun getPlants(): List<Plant> = dataSource.getPlants()
}
