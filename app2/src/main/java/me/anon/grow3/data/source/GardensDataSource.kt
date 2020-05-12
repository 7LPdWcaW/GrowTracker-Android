package me.anon.grow3.data.source

import me.anon.grow3.data.model.Garden

/**
 * // TODO: Add class description
 */
interface GardensDataSource
{
	enum class SyncDirection
	{
		SAVE,
		LOAD
	}

	suspend fun addGarden(garden: Garden): List<Garden>

	suspend fun getGardenById(gardenId: String): Garden?

	suspend fun getGardens(): List<Garden>

	suspend fun sync(direction: SyncDirection = SyncDirection.SAVE): List<Garden>
}
