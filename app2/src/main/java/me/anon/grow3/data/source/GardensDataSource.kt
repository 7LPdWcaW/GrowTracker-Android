package me.anon.grow3.data.source

import me.anon.grow3.data.model.Diary

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

	suspend fun addGarden(diary: Diary): List<Diary>

	suspend fun getGardenById(gardenId: String): Diary?

	suspend fun getGardens(): List<Diary>

	suspend fun sync(direction: SyncDirection = SyncDirection.SAVE): List<Diary>
}
