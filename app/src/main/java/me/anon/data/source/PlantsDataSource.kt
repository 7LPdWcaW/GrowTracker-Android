package me.anon.data.source

import androidx.lifecycle.LiveData
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
interface PlantsDataSource
{
	public fun loaded(): LiveData<Result<Boolean>>

	public fun addPlant(plant: Plant)
	public fun setPlant(plant: Plant)

	public val plants: LiveData<List<Plant>>
	suspend fun getPlants(): List<Plant>
	suspend fun getPlantById(id: String): Plant?

	suspend fun save()
}
