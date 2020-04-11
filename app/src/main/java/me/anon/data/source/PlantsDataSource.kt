package me.anon.data.source

import androidx.lifecycle.LiveData
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
interface PlantsDataSource
{
	public fun addPlant(plant: Plant)
	public fun setPlant(plant: Plant)
	suspend fun getPlants(): List<Plant>
	public fun observePlants(): LiveData<List<Plant>>
	suspend fun save()
}
