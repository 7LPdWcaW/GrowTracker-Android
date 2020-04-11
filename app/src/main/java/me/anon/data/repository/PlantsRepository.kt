package me.anon.data.repository

import androidx.lifecycle.LiveData
import me.anon.model.Plant

/**
 * // TODO: Add class description
 */
interface PlantsRepository
{
	suspend fun reload()

	public fun observePlants(): LiveData<List<Plant>>

	suspend fun getPlants(): List<Plant>

	public fun addPlant(plant: Plant)
	public fun setPlant(plant: Plant)

	suspend fun save()
}
