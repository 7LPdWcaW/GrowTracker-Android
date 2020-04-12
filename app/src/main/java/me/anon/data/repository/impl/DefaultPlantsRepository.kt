package me.anon.data.repository.impl

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.anon.data.repository.PlantsRepository
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
	override fun loaded(): LiveData<Result<Boolean>> = dataSource.loaded()

	override suspend fun reload()
	{
		dataSource.getPlants()
	}

	override fun addPlant(plant: Plant)
	{
		dataSource.addPlant(plant)
	}

	override fun setPlant(plant: Plant)
	{
		dataSource.setPlant(plant)
	}

	override suspend fun save()
	{
		coroutineScope {
			launch { dataSource.save() }
		}
	}

	override fun observePlants(): LiveData<List<Plant>> = dataSource.observePlants()
	override suspend fun getPlants(): List<Plant> = dataSource.getPlants()
}
