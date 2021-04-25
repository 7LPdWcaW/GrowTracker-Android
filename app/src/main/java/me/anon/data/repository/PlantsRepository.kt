package me.anon.data.repository

import androidx.lifecycle.LiveData
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
interface PlantsRepository
{
	public fun loaded(): LiveData<Result<Boolean>>
	suspend fun reload()

	public val plants: LiveData<List<Plant>>
	suspend fun getPlants(): List<Plant>

	suspend fun getPlantById(id: String): Plant?
	public fun getPlantsById(vararg id: String): LiveData<List<Plant>>
	public fun getPlantsByGarden(gardenId: String): LiveData<List<Plant>>

	public fun addPlant(plant: Plant)
	public fun setPlant(plant: Plant)

	suspend fun save()
}
