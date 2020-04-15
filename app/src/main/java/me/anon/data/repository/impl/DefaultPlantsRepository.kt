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
		dataSource.plants
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

	override val plants: LiveData<List<Plant>> = dataSource.plants
	override suspend fun getPlants(): List<Plant> = dataSource.getPlants()
	override suspend fun getPlantById(id: String): Plant? = dataSource.getPlantById(id)

	override fun getPlantsById(vararg id: String): LiveData<List<Plant>>
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getPlantsByGarden(gardenId: String): LiveData<List<Plant>>
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}
